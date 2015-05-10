package de.sekmi.histream.i2b2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;







import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.StoredExtensionType;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.ext.PatientStore;

/**
 * Patient cache which synchronizes with i2b2 patient_dimension and patient_mapping tables.
 * Required non-null columns are patient_num, update_date.
 * <p>
 * The method {@link #open()} will load all patients (relevant to the selected project)
 * into a memory structure based on a Hashtable.
 * <p>
 * Some optional columns are used: vital_status_cd, birth_date, death_date, sex_cd, download_date, sourcesystem_cd
 * <p>
 * The store should be separate from the extension? E.g. I2b2PatientExtension which can operate
 * on PostgresStore, OracleStore, etc.
 * HistreamPatientExtension can also operate on I2b2PostgresStore?
 * <p>
 * The patient_mapping table is used to map actual (source) patient_ide to internal patient_num for facts.
 * The patient_mapping table is also used to store patient merge events (eg. different patient_ide referring to the same patient_num
 * The Observation stream will still use the actual source patient_ide and encounter_ide
 * <p>
 * In the patient_mapping table, patient_ide_status can assume values Active, Inactive, Deleted, Merged.
 * The 'Active' patient_mapping for the selected project is used as primary patient id, 
 * all other rows are used as alias ids.
 * 
 * @author marap1
 *
 */
public class PostgresPatientStore extends PostgresExtension<I2b2Patient> implements PatientStore{
	private static final Logger log = Logger.getLogger(PostgresPatientStore.class.getName());
	private static final Class<?>[] INSTANCE_TYPES = new Class<?>[]{Patient.class, I2b2Patient.class};
	private String projectId;
	private String idSourceDefault;
	private char idSourceSeparator;
	
//	private String autoInsertSourceId;

	// maximum patient number, used to generate new patient_num for new patients
	private int maxPatientNum;
	
	//private static ChronoUnit[] map_date_units = {ChronoUnit.DAYS, ChronoUnit.MONTHS, ChronoUnit.YEARS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS};
	//private static char[] map_death_chars = {};
	private Hashtable<Integer, I2b2Patient> patientCache;
	private Hashtable<String, I2b2Patient> idCache;
	
	private PreparedStatement insert;
	private PreparedStatement insertIde;
	
	private PreparedStatement update;
	/*
	private PreparedStatement select;
	private PreparedStatement selectIde;
	*/
	private PreparedStatement selectAll;
	private PreparedStatement selectAllIde;
	private PreparedStatement deletePatientSource;
	private PreparedStatement deleteMapSource;
	
	/**
	 * Construct new postgres patient store. In addition to properties
	 * needed by {@link PostgresExtension#PostgresExtension(Properties), the
	 * following properties are needed: 
	 * <p>projectId, 
	 * <p>Optional properties:
	 * <p>
	 * 	idSourceDefault ('HIVE'), idSourceSeparator (single char, ':')
	 * 	fetchSize (int, 10000)
	 * @param configuration
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public PostgresPatientStore(Map<String,String> configuration) throws ClassNotFoundException, SQLException {
		super(configuration);
		this.projectId = config.get("project");
		if( projectId == null ){
			log.warning("property project is null, some things might fail");
		}
		this.idSourceDefault = "HIVE";
		this.idSourceSeparator = ':';
//		this.autoInsertSourceId = "HS.auto";
		patientCache = new Hashtable<>(1000);
		idCache = new Hashtable<>(1000);
		open();
	}
	
	private I2b2Patient getCached(int patient_num){
		return patientCache.get(patient_num);
	}
	
	private I2b2Patient getCached(String patient_id){
		return idCache.get(patient_id);
	}


	private void loadMaxPatientNum() throws SQLException{
		Statement s = db.createStatement();
		String sql = "SELECT MAX(patient_num) FROM patient_dimension";
		try( ResultSet rs = s.executeQuery(sql) ){
			if( rs.next() ){
				maxPatientNum = rs.getInt(1);
			}else{
				// patient_dimension is empty
				// start numbering patients with 1
				maxPatientNum = 1;
			}
		}
		log.info("MAX(patient_num) = "+maxPatientNum);
	}

	@Override
	protected void prepareStatements()throws SQLException{

		db.setAutoCommit(true);
		// TODO: use prefix from configuration to specify tablespace
		insert = db.prepareStatement("INSERT INTO patient_dimension(patient_num, import_date, sourcesystem_cd) VALUES(?,current_timestamp,?)");
		insertIde = db.prepareStatement("INSERT INTO patient_mapping(patient_ide, patient_ide_source, patient_num, patient_ide_status, project_id, import_date, download_date, sourcesystem_cd) values (?,?,?,?,'"+projectId+"',current_timestamp,?,?)");
		update = db.prepareStatement("UPDATE patient_dimension SET vital_status_cd=?, birth_date=?, death_date=?, sex_cd=?, update_date=current_timestamp, download_date=?, sourcesystem_cd=? WHERE patient_num=?");
		
		/*
		selectIde = db.prepareStatement("SELECT m.patient_num, m.patient_ide_status FROM patient_mapping m WHERE m.patient_ide=? AND patient_ide_source=? AND m.project_id='"+projectId+"'");
		select = db.prepareStatement("SELECT p.patient_num, p.vital_status_cd, p.birth_date, p.death_date, p.sex_cd, p.download_date, p.sourcesystem_cd, m.patient_ide, m.patient_ide_source, m.patient_ide_status FROM patient_mapping m, patient_dimension p WHERE m.patient_num=p.patient_num AND m.patient_ide=? AND patient_ide_source=? AND m.project_id='"+projectId+"'");
		*/
		//selectAll = db.prepareStatement("SELECT p.patient_num, p.vital_status_cd, p.birth_date, p.death_date, p.sex_cd, p.download_date, p.sourcesystem_cd, m.patient_ide, m.patient_ide_source, m.patient_ide_status FROM patient_mapping m, patient_dimension p WHERE m.patient_num=p.patient_num AND m.project_id='"+projectId+"'");
		// TODO select only patients relevant to the current project: eg. join patient_dimension with patient_mapping to get only relevant rows.
		selectAll = db.prepareStatement("SELECT patient_num, vital_status_cd, birth_date, death_date, sex_cd, download_date, sourcesystem_cd FROM patient_dimension", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		selectAll.setFetchSize(getFetchSize());
		
		selectAllIde = db.prepareStatement("SELECT patient_num, patient_ide, patient_ide_source, patient_ide_status, project_id FROM patient_mapping WHERE project_id='"+projectId+"' ORDER BY patient_num", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		selectAllIde.setFetchSize(getFetchSize());
		
		deletePatientSource = db.prepareStatement("DELETE FROM patient_dimension WHERE sourcesystem_cd=?");
		deleteMapSource = db.prepareStatement("DELETE FROM patient_mapping WHERE sourcesystem_cd=?");
	}
	/**
	 * Returns the number of patients which are currently available in memory.
	 * The number might grow as more patients are loaded from the database.
	 * @return number of patients.
	 */
	public int size(){
		return patientCache.size();
	}
	@Override
	public void open()throws SQLException, ClassNotFoundException{
		super.open();

		loadMaxPatientNum();
		batchLoad();
	}
	
	private void batchLoad() throws SQLException{
		try( ResultSet rs = selectAll.executeQuery() ){
			while( rs.next() ){
				I2b2Patient patient = loadFromResultSet(rs);
				// put in numeric patient cache
				patientCache.put(patient.getNum(), patient);
			}
		}

		try( ResultSet rs = selectAllIde.executeQuery() ){
			I2b2Patient p;
			ArrayList<String> ids = new ArrayList<>(16);
			int num = 0; // current patient number
			while( rs.next() ){
				if( num == 0 )num = rs.getInt(1);
				else if( num != rs.getInt(1) ){
					// next patient
					// cache ids for current patients
					p = getCached(num);
					if( p == null ){
						// found row in patient_mapping which 
						// doesn't correspond to any row in patient_num
						log.warning("No match for patient_num="+num+" in patient_dimension (see encounter_mapping.id='"+rs.getString(2)+"'");
					}else if( ids.size() > 0 ){
						p.mergedIds = new String[ids.size()];
						p.mergedIds = ids.toArray(p.mergedIds);
					}
					// proceed with next patient
					num = rs.getInt(1);
					ids.clear();
				}
				String id;
				// no prefix for ids with source idSourceDefault
				if( rs.getString(3).equals(idSourceDefault) )
					id = rs.getString(2);
				else id = rs.getString(3)+idSourceSeparator+rs.getString(2);
				
				// active id in current project is used for patient.getId
				if( rs.getString(4).equals("A") && rs.getString(5).equals(projectId) ){
					p = getCached(num);
					if( p != null ){
						p.setId(id);
						p.markDirty(false);
					}
				}else // all other ids are aliases
					ids.add(id);
			}
			// don't forget to process last num
			p = getCached(num);
			if( p == null ){
				// found row in patient_mapping which 
				// doesn't correspond to any row in patient_num
				log.warning("No match for patient_num="+num+" in patient_dimension from encounter_mapping");
			}else if( ids.size() > 0 ){
				p.mergedIds = new String[ids.size()];
				p.mergedIds = ids.toArray(p.mergedIds);
			}
			
		}
		
		// fill idCache
		Enumeration<I2b2Patient> all = patientCache.elements();
		while( all.hasMoreElements() ){
			I2b2Patient p = all.nextElement();
			// XXX how make sure all patients have setId/getId set?
			if( p.getId() != null )idCache.put(p.getId(), p);
			if( p.mergedIds != null ){
				for( int i=0; i<p.mergedIds.length; i++ ){
					idCache.put(p.mergedIds[i], p);
				}
			}
			
		}
		
	}

	/*
	private I2b2Patient loadById(String id) throws IOException{
		IdWithSource ide = new IdWithSource(id);
		int patient_num;
		// lookup identity string
		synchronized( selectIde ){
			try {
				selectIde.setString(1, ide.ide);
				selectIde.setString(2, ide.ids);
			} catch (SQLException e) {
				throw new IOException(e);
			}
			try( ResultSet rs = selectIde.executeQuery() ){
				if( !rs.next() ) 
					return null;// patient not found
				patient_num = rs.getInt(1);
			}catch( SQLException e ){
				throw new IOException(e);
			}
			
		}
		I2b2Patient pat = getCached(patient_num);
		if( pat != null ){
			// patient already cached, return reference
			return pat;
		}
		// load patient with (internal) numeric id
		synchronized( select ){
			try {
				select.setInt(1, patient_num);
			} catch (NumberFormatException | SQLException e) {
				throw new IOException(e);
			}
			try( ResultSet rs = select.executeQuery() ){
				return loadFromResultSet(rs);
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}
	}
	*/
	
	private void updateStorage(I2b2Patient patient) throws SQLException {
			synchronized( update ){
				update.setString(1, getVitalStatusCd(patient));
				update.setTimestamp(2, inaccurateSqlTimestamp(patient.getBirthDate()));
				update.setTimestamp(3, inaccurateSqlTimestamp(patient.getDeathDate()));
				update.setString(4, getSexCd(patient));
				update.setTimestamp(5, Timestamp.from(patient.getSourceTimestamp()));
				update.setString(6, patient.getSourceId());
				update.setInt(7, patient.getNum());
				update.executeUpdate();
				patient.markDirty(false);
			}
	}


	/**
	 * Insert a new patient into the database. Only patient_num and sourcesystem_cd are filled.
	 * @param patient
	 * @throws SQLException
	 */
	private void insertPatient(I2b2Patient patient) throws SQLException{
		synchronized( insert ){
			insert.setInt(1, patient.getNum() );
			insert.setString(2, patient.getSourceId());
			insert.executeUpdate();
			patient.markDirty(false);
		}
	}
	
	/**
	 * Get the i2b2 sex_cd for a patient
	 * @param patient
	 * @return
	 */
	private static String getSexCd(Patient patient){
		if( patient.getSex() == null )return null;
		else switch( patient.getSex() ){
		case Female:
			return "F";
		case Male:
			return "M";
		default:
			// XXX should not happen, warning
			return null;
		}
	}
	/**
	 * Get the i2b2 vital_status_cd for a patient
	 * @param patient
	 * @return vital status code, see CRC_Design doc
	 */
	private static String getVitalStatusCd(Patient patient){
		char death_char=0, birth_char=0;
		if( patient.getDeathDate() != null ){
			switch( patient.getDeathDate().getAccuracy() ){
			case DAYS:
				death_char = 'Y';
				break;
			case MONTHS:
				death_char = 'M';
				break;
			case YEARS:
				death_char = 'X';
				break;
			case HOURS:
				death_char = 'R';
				break;
			case MINUTES:
				death_char = 'T';
				break;
			case SECONDS:
				death_char = 'S';
				break;
			default:
			}
		}

		// birth date
		if( patient.getBirthDate() != null ){
			switch( patient.getBirthDate().getAccuracy() ){
			case DAYS:
				death_char = 'D';
				break;
			case MONTHS:
				death_char = 'B';
				break;
			case YEARS:
				death_char = 'F';
				break;
			case HOURS:
				death_char = 'H';
				break;
			case MINUTES:
				death_char = 'I';
				break;
			case SECONDS:
				death_char = 'C';
				break;
			default:
			}
		}

		if( death_char != 0 && birth_char != 0 )
			return new String(new char[]{death_char,birth_char});
		else if( death_char != 0 )
			return new String(new char[]{death_char});
		else if( birth_char != 0 )
			return new String(new char[]{birth_char});
		else return null;
	}

	private void setVitalStatusCd(Patient patient, String vital_cd){
		// load accuracy
		if( vital_cd == null )return; // nothing to do
		
		ChronoUnit accuracy = null;
		char birthIndicator = 0;
		
		// death date indicator
		switch( vital_cd.charAt(0) ){
		case 'N': // living, no date
		case 'U': // unknown, no date
		case 'Z': // deceased, no date
			break;
		case 'Y': // deceased, accurate to day
			accuracy = ChronoUnit.DAYS;
			break;
		case 'M': // deceased, accurate to month
			accuracy = ChronoUnit.MONTHS;
			break;
		case 'X': // deceased, accurate to year
			accuracy = ChronoUnit.YEARS;
			break;
		case 'R': // deceased, accurate to hour
			accuracy = ChronoUnit.HOURS;
			break;
		case 'T': // deceased, accurate to minute
			accuracy = ChronoUnit.MINUTES;
			break;
		case 'S': // deceased, accurate to second
			accuracy = ChronoUnit.SECONDS;
			break;
		default:
			// no match for death status -> check for birth status in first character
			birthIndicator = vital_cd.charAt(0);
		}

		if( patient.getBirthDate() != null && accuracy != null ){
			patient.getBirthDate().setAccuracy(accuracy);
		}
		if( birthIndicator == 0 && vital_cd.length() > 1 )
			birthIndicator = vital_cd.charAt(1);
		// birth date indicator
		switch( birthIndicator ){
		case 'L': // unknown, no date
			break;
		case 'D': // known, accurate to day
			accuracy = ChronoUnit.DAYS;
			break;
		case 'B': // known, accurate to month
			accuracy = ChronoUnit.MONTHS;
			break;
		case 'F': // known, accurate to year
			accuracy = ChronoUnit.YEARS;
			break;
		case 'H': // known, accurate to hour
			accuracy = ChronoUnit.HOURS;
			break;
		case 'I': // known, accurate to minute
			accuracy = ChronoUnit.MINUTES;
			break;
		case 'C': // known, accurate to second
			accuracy = ChronoUnit.SECONDS;
			break;
		}
		if( patient.getBirthDate() != null && accuracy != null ){
			patient.getBirthDate().setAccuracy(accuracy);
		}		
	}

	private I2b2Patient loadFromResultSet(ResultSet rs) throws SQLException{
		int id = rs.getInt(1);
		
		// load vital status code, which contains information about
		// accuracy of birth and death dates.
		String vital_cd = rs.getString(2);
		// make sure that non-null vital code contains at least one character
		if( vital_cd == null || vital_cd.length() == 0 )vital_cd = null;
		
		DateTimeAccuracy birthDate = null;
		DateTimeAccuracy deathDate = null;
		// birth date
		Timestamp ts = rs.getTimestamp(3);
		if( ts != null ){
			birthDate = new DateTimeAccuracy(ts.toLocalDateTime());
		}
		// death date
		ts = rs.getTimestamp(4);
		if( ts != null ){
			deathDate = new DateTimeAccuracy(ts.toLocalDateTime());
		}
		
		// load sex
		String sex_cd = rs.getString(5);
		Sex sex = null;
		if( sex_cd != null ){
			switch( sex_cd.charAt(0) ){
			case 'F':
				sex = Sex.Female;
				break;
			case 'M':
				sex = Sex.Male;
				break;
			}
		}
		
		I2b2Patient patient = new I2b2Patient(id, sex, birthDate, deathDate);
		if( rs.getTimestamp(6) != null )
			patient.setSourceTimestamp(rs.getTimestamp(6).toInstant());
		
		patient.setSourceId(rs.getString(7));
		
		setVitalStatusCd(patient, vital_cd);
		
		patient.markDirty(false);
		
		return patient;
	}
	/*
	private void retrievalException(String id, IOException e) {
		log.log(Level.SEVERE, "Unable to retrieve patient "+id, e);
	}*/

	private void insertionException(I2b2Patient patient, SQLException e) {
		log.log(Level.SEVERE, "Unable to insert patient "+patient.getId(), e);
	}

	
	private void updateException(I2b2Patient patient, SQLException e) {
		log.log(Level.SEVERE, "Unable to update patient "+patient.getId(), e);
	}

	private String[] splitId(String id){
		String ide;
		String ids;
		int p = id.indexOf(idSourceSeparator);
		if( p == -1 ){
			// id does not contain source
			ids = idSourceDefault;
			ide = id;
		}else{
			// id contains source, separate from id
			ids = id.substring(0, p);
			ide = id.substring(p+1);
		}
		return new String[]{ids,ide};
	}
	
	
	private void insertIde(int patient_num, String id, String status, ExternalSourceType source)throws SQLException{
		String[] ids = splitId(id);
		insertIde.setString(1, ids[1]);
		insertIde.setString(2, ids[0]);
		insertIde.setInt(3, patient_num);
		insertIde.setString(4, status);
		insertIde.setTimestamp(5, Timestamp.from(source.getSourceTimestamp()));
		insertIde.setString(6, source.getSourceId());
		insertIde.executeUpdate();
	}
	@Override
	public I2b2Patient createInstance(Observation fact) {

		
		I2b2Patient pat = getCached(fact.getPatientId());
		if( pat == null ){
			// string id not known to cache
			// create new patient
			maxPatientNum ++;
			int num = maxPatientNum;
			pat = new I2b2Patient(num);
			pat.setId(fact.getPatientId());
			
			
			// don't use source metadata, since we only know the patient id
			pat.setSourceId(fact.getSourceId());
			//pat.setSourceTimestamp(fact.getSourceTimestamp());

			// put in cache and insert into storage
			patientCache.put(num, pat);
			idCache.put(pat.getId(), pat);
			try {
				insertPatient(pat);
				
				// insert ide into patient_mapping
				insertIde(num, pat.getId(), "A", fact);
			} catch (SQLException e) {
				insertionException(pat, e);
			} 
			
			// commonly, the item is modified after a call to this method,
			// but changes are written later via a call to update.
			// (otherwise, the instance would need to know whether to perform INSERT or UPDATE)
		}
		return pat;
	}

	@Override
	public Class<?>[] getInstanceTypes() {
		return INSTANCE_TYPES;
	}

	@Override
	public I2b2Patient createInstance() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Patient retrieve(String id) {
		return idCache.get(id);
	}

	@SuppressWarnings("unused")
	@Override
	public void merge(Patient patient, String additionalId, ExternalSourceType source) {
		I2b2Patient p = (I2b2Patient)patient;
		if( true )throw new UnsupportedOperationException();
		// TODO add additionalId to patient.mergedIds 
		
		try {
			insertIde(p.getNum(), additionalId, "M", source);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Unable to insert patient merge", e);
		}
	}

	@Override
	public String[] getAliasIds(Patient patient) {
		I2b2Patient p = (I2b2Patient)patient;
		return p.mergedIds;
	}
	

	// TODO unify method with PostgresVisitStore
	@Override
	public void deleteWhereSourceId(String sourceId) throws SQLException {
		deletePatientSource.setString(1, sourceId);
		int numRows = deletePatientSource.executeUpdate();
		log.info("Deleted "+numRows+" rows with sourcesystem_cd = "+sourceId);

		deleteMapSource.setString(1, sourceId);
		deleteMapSource.executeUpdate();
		// find matching patients in cache
		Enumeration<I2b2Patient> all = patientCache.elements();
		LinkedList<I2b2Patient>remove = new LinkedList<>();
		while( all.hasMoreElements() ){
			I2b2Patient p = all.nextElement();
			if( p.getSourceId() != null && p.getSourceId().equals(sourceId) ){
				remove.add(p); // remove later, otherwise the Enumeration might fail
			}
			// XXX does not work with sourceId == null
		}
		// remove patients from cache
		for( I2b2Patient p : remove ){
			patientCache.remove(p.getNum());
			if( p.getId() != null )
				idCache.remove(p.getId());
			
			if( p.mergedIds != null )for( String id : p.mergedIds ){
				idCache.remove(id);
			}
		}
		// XXX some ids might remain in patient_mapping, because we don't store the patient_mapping sourcesystem_cd
		// usually this should work, as we assume sourcesystem_cd to be equal for patients in both tables
		
		// reload MAX(patient_num)
		loadMaxPatientNum();
	}
	

	@Override
	public void flush(){
		int count = 0;
		Iterator<I2b2Patient> dirty = StoredExtensionType.dirtyIterator(patientCache.elements());
		while( dirty.hasNext() ){
			I2b2Patient patient = dirty.next();
			try {
				updateStorage(patient);
				count ++;
			} catch (SQLException e) {
				updateException(patient, e);
			}
		}
		if( count != 0 )log.info("Updated "+count+" patients in database");
	}

	@Override
	public void purge(String id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
	
}
