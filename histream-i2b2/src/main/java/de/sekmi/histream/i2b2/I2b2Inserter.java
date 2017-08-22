package de.sekmi.histream.i2b2;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;


import de.sekmi.histream.Modifier;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationHandler;
import de.sekmi.histream.Plugin;
import de.sekmi.histream.Value;
import de.sekmi.histream.impl.AbstractObservationHandler;

/**
 * Inserts observations in to the i2b2 observation_fact table.
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
 * Timestamp and datetime values are stored without timezone information. The timezone
 * which should be used when reading/writing to database can be specified via
 * the {@link DataDialect} param in {@link #open(Connection, DataDialect)}.
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
 * @author R.W.Majeed
 *
 */
public class I2b2Inserter extends AbstractObservationHandler implements ObservationHandler, Closeable, Plugin{
	private static final Logger log = Logger.getLogger(I2b2Inserter.class.getName());
	private Connection db;
	private PreparedStatement insertFact;
	private PreparedStatement deleteSource;
	private PreparedStatement deleteVisit;
	private Preprocessor etlPreprocessor;
	private DataDialect dialect;
	private int insertCount;
	
//	public I2b2Inserter(Map<String,String> config) throws ClassNotFoundException, SQLException{
//		db = PostgresExtension.getConnection(config, new String[]{"jdbc.","data.jdbc."});
//		initialize(config);
//	}
	
//	public I2b2Inserter(DataSource ds, Map<String,String> config) throws SQLException{
//		db = ds.getConnection();
//		initialize(config);
//	}
	public I2b2Inserter(){
	}
	private interface Preprocessor{
		void preprocess(Observation fact)throws SQLException;
	}
	private class DistinctVisitPurge implements Preprocessor{
		private I2b2Visit prev;

		@Override
		public void preprocess(Observation fact) throws SQLException{
			I2b2Visit current = fact.getExtension(I2b2Visit.class);
			if( current != prev ){
				purgeVisit(current.getNum());
				prev = current;
			}
		}
	}
	private class UniqueSourcePurge implements Preprocessor{
		private Set<String> purgedSources;
		public UniqueSourcePurge(){
			purgedSources = new HashSet<>();
		}
		@Override
		public void preprocess(Observation fact) throws SQLException {
			String sourceId = fact.getSource().getSourceId();
			if( !purgedSources.contains(sourceId) ){
				purgedSources.add(sourceId);
				purgeSource(sourceId);
			}
			
		}
	}
	/**
	 * Deletes all observations with the given sourceId
	 * @param sourceId source id
	 * @return {@code true} if observations were found and deleted, {@code false} if no matching observations were found.
	 * @throws SQLException if the DELETE statement failed
	 */
	public synchronized boolean purgeSource(String sourceId)throws SQLException{
		deleteSource.setString(1, sourceId);
		int rows = deleteSource.executeUpdate();
		db.commit();
		log.info("Deleted "+rows+" rows for sourcesystem_cd="+sourceId);
		return 0 != rows;
	}
	
	/**
	 * Deletes all observations for the given encounter_num
	 * @param encounter_num encounter number (e.g. observation_fact.encounter_num)
	 * @return {@code true} if observations were found and deleted, {@code false} if no matching observations were found.
	 * @throws SQLException if the DELETE statement failed.
	 */
	public synchronized boolean purgeVisit(int encounter_num) throws SQLException{
		deleteVisit.setInt(1, encounter_num);
		int rows = deleteVisit.executeUpdate();
		db.commit();
		log.info("Deleted "+rows+" rows for encounter_num="+encounter_num);
		return 0 != rows;
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
	 * Initialize the database connection
	 * @param connection database connection for insertions
	 * @param dialect data dialect for i2b2
	 * @throws SQLException if preparation/initialisation failed
	 */
	public void open(Connection connection, DataDialect dialect)throws SQLException{
		this.dialect = dialect;
		this.db = connection;
//		String nullProvider = props.get("nullProvider");
//		if( nullProvider == null ){
//			log.warning("property 'nullProvider' missing, using '@' (may violate foreign keys)");
//			nullProvider = "@";
//		}
		
		insertCount = 0;
		db.setAutoCommit(false);
		prepareStatements();
	}

	
	@Override
	public void acceptOrException(Observation o) throws ObservationException{
		if( etlPreprocessor != null ){
			try{
				etlPreprocessor.preprocess(o);
			}catch( SQLException e ){
				throw new ObservationException("ETL preprocessing failed", e);
			}
		}
			
		try {
			
			insertFact(o, null, 1);
			db.commit();
			
			insertCount ++;
		} catch (SQLException e) {
			try {
				db.rollback();
			} catch (SQLException suppressed) {
				e.addSuppressed(suppressed);
			}
			throw new ObservationException("Insert failed",e);
		}
		
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
			// i2b2 encounter not available, try to parse the encounter id as number
			return Integer.parseInt(o.getEncounterId());
		}//*/finally{}
	}

	/**
	 * Insert a fact into the observation_fact table, including all associated modifiers.
	 * <p>
	 * This method makes use of database transactions to ensure only complete facts are inserted.
	 * Therefore, {@code db.commit()} should be called following a call to this function. If any exception
	 * is encountered, {@code db.rollback()} should be called.
	 * @param o Observation/fact to insert
	 * @param m Modifier to insert for the fact. Should be {@code null} and is only used during recursion.
	 * @param instanceNum instance num
	 * @throws SQLException exception during insert. Call {@code db.rollback()} if caught.
	 */
	private synchronized void insertFact(Observation o, Modifier m, int instanceNum)throws SQLException{
		if( m == null && o.hasModifiers() ){
			// method called for observation (not modifier) with modifiers
			// get new instance number
			instanceNum = incrementInstanceNum(o);
		}

		insertFact.setInt(1, getEncounterNum(o));
		insertFact.setInt(2, getPatientNum(o));
		insertFact.setString(3, o.getConceptId());
		insertFact.setString(4, dialect.encodeProviderId(o.getProviderId()));
		// start_date
		Objects.requireNonNull(o.getStartTime());
		insertFact.setTimestamp(5, dialect.encodeInstant(o.getStartTime().toInstantMin()));
		
		insertFact.setString(6, (m==null)?dialect.getNullModifierCd():m.getConceptId());
		insertFact.setInt(7, instanceNum);

		Value v = (m==null)?o.getValue():m.getValue();
		if( v == null ){
			// valtype_cd
			insertFact.setString(8, dialect.getNullValueTypeCd());
			// tval_char
			insertFact.setString(9, null);
			// nval_num
			insertFact.setBigDecimal(10, null);
			// value_flag_cd
			insertFact.setString(11, dialect.getNullValueFlagCd());
			// units_cd
			insertFact.setString(12, dialect.getNullUnitCd());
		}else{
			switch( v.getType() ){
			case Numeric:
				// valtype_cd
				insertFact.setString(8, "N");
				// tval_char
				insertFact.setString(9, dialect.encodeOperator(v));
				// nval_num
				insertFact.setBigDecimal(10, v.getNumericValue());
				// value_flag_cd
				insertFact.setString(11, dialect.encodeValueFlagCd(v));
				// units_cd
				insertFact.setString(12, dialect.encodeUnitCd(v.getUnits()));
				break;
			case Text:
				// valtype_cd
				insertFact.setString(8, "T");
				// tval_char
				insertFact.setString(9, v.getStringValue());
				// nval_num
				insertFact.setBigDecimal(10, null);
				// value_flag_cd
				insertFact.setString(11, dialect.encodeValueFlagCd(v));
				// units_cd
				insertFact.setString(12, dialect.encodeUnitCd(v.getUnits()));
				break;
			default:
				throw new UnsupportedOperationException("Incomplete refactoring, unsupported value type "+v.getType());
			}
		}
		// end_date
		if( o.getEndTime() == null ){
			insertFact.setTimestamp(13, null);
		}else{
			insertFact.setTimestamp(13, dialect.encodeInstant(o.getEndTime().toInstantMin()));
		}
		// location_cd
		insertFact.setString(14, dialect.encodeLocationCd(o.getLocationId()));
		// download_date
		insertFact.setTimestamp(15, dialect.encodeInstant(o.getSource().getSourceTimestamp()));
		insertFact.setString(16, o.getSource().getSourceId());
		
		insertFact.executeUpdate();
		
		if( o.hasModifiers() == false ){
			// no modifiers involved, transaction done
		}else if( m == null ){
			// insert all modifiers
			// loop through modifiers
			Iterator<Modifier> e = o.getModifiers();
			while( e.hasNext() ){
				Modifier mod = e.next();
				insertFact(o, mod, instanceNum);
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
		log.info("Inserted "+insertCount+" facts");
	}

	@Override
	public void setMeta(String key, String value) {
		Objects.requireNonNull(key);
		if( key.equals("etl.strategy") ){
			// use default strategy 'insert' for null values
			if( value == null ){
				value = "insert";
			}
			switch( value ){
			case "replace-visit":
				etlPreprocessor = new DistinctVisitPurge();
				break;
			case "replace-source":
				etlPreprocessor = new UniqueSourcePurge();
				break;
			case "insert":
				etlPreprocessor = null;
				break;
			default:
				throw new IllegalArgumentException("Unknown etl strategy "+value);
			}
		}else{
			throw new IllegalArgumentException("Unknown meta key "+key);
		}
	}
	
	public int getInsertCount(){
		return insertCount;
	}

	public void resetInsertCount(){
		this.insertCount = 0;
	}
}
