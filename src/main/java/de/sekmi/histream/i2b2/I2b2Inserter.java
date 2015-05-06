package de.sekmi.histream.i2b2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.sekmi.histream.Modifier;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationHandler;
import de.sekmi.histream.Value;

/**
 * Inserts observtions in to the i2b2 observation_fact table.
 * <p>
 * Need to specify default provider id for i2b2 in property 'nullProvider'.
 * Null values in providerId will be be replaced with this value and stored in
 * provider_id column.
 * <p>
 * locationId is stored as location_cd, null-values are stored as '@'.
 * 
 * <p>
 * valtype_cd: N numeric, B stored in observation_blob, T text, '@' no value, 'NLP' NLP result xml objects.
 * Undocumented but used in demodata: D: datetime "YYYY-MM-DD HH:mm" stored in tval_char, "YYYYMMDD.HHmm0" stored in nval_num.
 * <p>
 * The most difficult part is handling the instance_num field.
 * By default, i2b2 uses a four byte signed integer for instance_num. Incrementing
 * instance_num for every record would lead eventually to a number overflow.
 * The primary key consists of patient_num,concept_cd,modifier_cd,start_date,encounter_num,instance_num,provider_id.
 * Ideally, instance_num should be unique to encounter_num and start_date, but there
 * is no realistic way to keep track per start_date, since observations can come in
 * in any order. Therefore, we keep track of the maximum instance_num per encounter
 * in the visit store (which caches visits anyways) and increase the instance_num only
 * for observations with modifiers.
 * 
 * @author marap1
 *
 */
public class I2b2Inserter implements ObservationHandler, Closeable{
	private static final Logger log = Logger.getLogger(I2b2Inserter.class.getName());
	private Connection db;
	private PreparedStatement insertFact;
	private PreparedStatement deleteSource;
	private PreparedStatement deleteVisit;
	private String nullProviderId;
	private String nullUnitCd;
	private String nullLocationCd;
	private String nullModifierCd;
	private String nullValueFlagCd;
	
	public I2b2Inserter(){
		this.nullUnitCd = "@"; // technically, null is allowed, but the demodata uses both '@' and ''
		this.nullLocationCd = "@"; // technically, null is allowed, but the demodata only uses '@'
		this.nullValueFlagCd = "@";// technically, null is allowed, but the demodata uses both '@' and ''
		// TODO nullBlob (technically null allowed, but '' is used in demodata)
		this.nullModifierCd = "@"; // null not allowed, @ is used in demodata
	}
	
	/**
	 * Deletes all observations with the given sourceId
	 * @param sourceId
	 * @throws SQLException
	 */
	public synchronized void purgeSource(String sourceId)throws SQLException{
		deleteSource.setString(1, sourceId);
		deleteSource.executeUpdate();
		db.commit();
	}
	
	/**
	 * Deletes all observations for the given encounter_num
	 * @param encounter_num
	 * @throws SQLException
	 */
	public synchronized void purgeVisit(int encounter_num) throws SQLException{
		deleteVisit.setInt(1, encounter_num);
		int rows = deleteVisit.executeUpdate();
		db.commit();
		log.info("Deleted "+rows+" observations for encounter_num="+encounter_num);
	}
	private void prepareStatements()throws SQLException{
		// no value
		insertFact = db.prepareStatement(""
				+ "INSERT INTO observation_fact ("
				+ "encounter_num, patient_num, concept_cd, provider_id, "
				+ "start_date, modifier_cd, instance_num, valtype_cd, "
				+ "tval_char, nval_num, valueflag_cd, units_cd, end_date, location_cd, "
				+ "update_date, download_date, import_date, sourcesystem_cd, upload_id"
				+ ") VALUES ("
				+ "?, ?, ?, ?, "
				+ "?, ?, ?, ?,"
				+ "?, ?, ?, ?, ?, ?,"
				+ "current_timestamp, ?, current_timestamp, ?, NULL)");

		deleteSource = db.prepareStatement("DELETE FROM observation_fact WHERE sourcesystem_cd=?");
		// TODO: verify index usage for delete by encounter_num
		deleteVisit = db.prepareStatement("DELETE FROM observation_fact WHERE encounter_num=?");
		
	}
	/**
	 * Opens a database connection and prepares statements
	 * @throws SQLException
	 */
	public void open()throws SQLException{
		Properties props = new Properties();
		props.put("user", "i2b2demodata");
		props.put("host", "localhost");
		props.put("database", "i2b2");
		props.put("port", "15432");
		props.put("password", "");
		props.put("nullProvider", "LCS-I2B2:PROVIDERS");
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
		db = DriverManager.getConnection("jdbc:postgresql://"+props.getProperty("host")+":"+props.getProperty("port")+"/"+props.getProperty("database"), props);
		db.setAutoCommit(false);
		this.nullProviderId = props.getProperty("nullProvider");
		prepareStatements();
		
	}

	
	@Override
	public void accept(Observation o) {
		// TODO Auto-generated method stub
		try {
			insertFact(o, null, 1);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Unable to insert fact", e);
		}
		
	}
	
	private static String getI2b2Operator(Value value){
		if( value.getOperator() == null )return "E";
		String op;
		switch( value.getOperator() ){
		case Equal:
			op = "E";
			break;
		case NotEqual:
			op = "NE";
			break;
		case LessThan:
			op = "L";
			break;
		case LessOrEqual:
			op = "LE";
			break;
		case GreaterThan:
			op = "G";
			break;
		case GreaterOrEqual:
			op = "GE";
			break;
		default:
			// TODO issue warning
			op = "E";
		}
		return op;
	}
	
	private static String getI2b2ValueFlagCd(Value value){
		String flag;
		if( value.getAbnormalFlag() == null )flag = null;
		else switch( value.getAbnormalFlag() ){
		case Abnormal:
			flag = "A";
			// TODO all flags
		default:
			flag = null;
		}
		return flag;
	}
	private int incrementInstanceNum(Observation o){
		try{
			I2b2Visit v = o.getExtension(I2b2Visit.class);
			v.maxInstanceNum ++;
			return v.maxInstanceNum;
		}catch( IllegalArgumentException e ){
			// i2b2 visit not available
			// no support for instance numbers, use default 1
			return 1;
		}
	}
	
	private static <T> T replaceNull(T value, T nullReplacement){
		return (value==null)?nullReplacement:value;
	}
	
	
	private int getPatientNum(Observation o){
		try{
			I2b2Patient patient = o.getExtension(I2b2Patient.class);
			return patient.getNum();
		}catch( IllegalArgumentException e ){
			// i2b2 patient not available, try to parse the patient id as number
			return Integer.parseInt(o.getPatientId());
		}
	}

	private int getEncounterNum(Observation o){
		try{
			I2b2Visit visit = o.getExtension(I2b2Visit.class);
			return visit.getNum();
		}catch( IllegalArgumentException e ){
			// i2b2 patient not available, try to parse the patient id as number
			return Integer.parseInt(o.getPatientId());
		}
	}

	private synchronized void insertFact(Observation o, Modifier m, int instanceNum)throws SQLException{
		if( m == null && o.hasModifiers() ){
			// method called for observation (not modifier) with modifiers
			// get new instance number
			instanceNum = incrementInstanceNum(o);
		}

		insertFact.setInt(1, getEncounterNum(o));
		insertFact.setInt(2, getPatientNum(o));
		insertFact.setString(3, o.getConceptId());
		insertFact.setString(4, replaceNull(o.getProviderId(),nullProviderId));
		// start_date
		insertFact.setTimestamp(5, Timestamp.valueOf(o.getStartTime().getLocal()));
		
		insertFact.setString(6, (m==null)?nullModifierCd:m.getConceptId());
		insertFact.setInt(7, instanceNum);

		Value v = (m==null)?o.getValue():m.getValue();
		switch( v.getType() ){
		case Numeric:
			// valtype_cd
			insertFact.setString(8, "N");
			// tval_char
			insertFact.setString(9, getI2b2Operator(v));
			// nval_num
			insertFact.setBigDecimal(10, v.getNumericValue());
			// value_flag_cd
			insertFact.setString(11, getI2b2ValueFlagCd(v));
			// units_cd
			insertFact.setString(12, replaceNull(v.getUnits(),nullUnitCd));
			break;
		case Text:
			// valtype_cd
			insertFact.setString(8, "T");
			// tval_char
			insertFact.setString(9, v.getValue());
			// nval_num
			insertFact.setBigDecimal(10, null);
			// value_flag_cd
			insertFact.setString(11, getI2b2ValueFlagCd(v));
			// units_cd
			insertFact.setString(12, replaceNull(v.getUnits(),nullUnitCd));
			break;
		case None:
			// valtype_cd
			insertFact.setString(8, "@");
			// tval_char
			insertFact.setString(9, null);
			// nval_num
			insertFact.setBigDecimal(10, null);
			// value_flag_cd
			insertFact.setString(11, nullValueFlagCd);
			// units_cd
			insertFact.setString(12, nullUnitCd);
			break;
		default:
			throw new UnsupportedOperationException("Incomplete refactoring, unsupported value type "+v.getType());
		}
		// end_date
		if( o.getEndTime() == null ){
			insertFact.setTimestamp(13, null);
		}else{
			insertFact.setTimestamp(13, Timestamp.valueOf(o.getEndTime().getLocal()));
		}
		// location_cd
		insertFact.setString(14, replaceNull(o.getLocationId(),nullLocationCd));
		// download_date
		insertFact.setTimestamp(15, Timestamp.from(o.getSourceTimestamp()));
		insertFact.setString(16, o.getSourceId());
		
		insertFact.executeUpdate();
		
		if( o.hasModifiers() == false ){
			// no modifiers involved, commit immediately
			db.commit();
		}else if( m == null ){
			// insert all modifiers and commit thereafter
			try{
				// loop through modifiers
				Enumeration<Modifier> e = o.getModifiers();
				while( e.hasMoreElements() ){
					Modifier mod = e.nextElement();
					insertFact(o, mod, instanceNum);
				}
				db.commit();
			}catch( SQLException e ){
				// error during modifier insertion
				db.rollback();
				log.warning("Rollback performed");
				throw e; // exception is handled upstream
			}
			
		}else{
			// just inserted a modifier, wait with commit
		}
		
	}

	@Override
	public void close() throws IOException {
		try {
			db.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

}
