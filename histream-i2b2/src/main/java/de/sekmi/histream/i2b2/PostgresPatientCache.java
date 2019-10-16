package de.sekmi.histream.i2b2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

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


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.StoredExtensionType;
import de.sekmi.histream.impl.ExternalSourceImpl;
import de.sekmi.histream.ext.Patient.Sex;

/**
 * Patient cache which synchronizes with i2b2 patient_dimension and patient_mapping tables.
 * Required non-null columns are patient_num, update_date.
 * <p>
 * The method {@link #open(Connection, String, DataDialect)} will load all patients (relevant to the selected project)
 * into a memory structure based on a hash table.
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
public class PostgresPatientCache implements Closeable{
	private static final Logger log = Logger.getLogger(PostgresPatientCache.class.getName());
	protected String projectId;
	protected String idSourceDefault;
	protected char idSourceSeparator;
	protected Connection db;
	protected int fetchSize;
	// TODO read only flag!!!!!! XXX
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

	protected DataDialect dialect;
	
//	/**
//	 * Construct new postgres patient store. In addition to properties
//	 * needed by {@link PostgresExtension#PostgresExtension(Map)},
//	 * the following properties are needed: 
//	 * <p>jdbc.{host|port|database} or data.jdbc.{host|port|database} to
//	 * construct the database URI.
//	 * Any other parameters under jdbc. or data.jdbc. are passed to the 
//	 * JDBC connect method.
//	 * 
//	 * <p>project, 
//	 * <p>Optional properties:
//	 * <p>
//	 * 	idSourceDefault ('HIVE'), idSourceSeparator (single char, ':')
//	 * 	fetchSize (int, 10000)
//	 * @param configuration configuration
//	 * @throws SQLException if the preparation or initialisation fails
//	 * @throws ClassNotFoundException if postgresql driver not found
//	 */
//	public PostgresPatientStore(Map<String,String> configuration) throws ClassNotFoundException, SQLException {
//		super(configuration);
//		this.projectId = config.get("project");
//		openDatabase(new String[]{"jdbc.","data.jdbc."});
//		initialize();
//	}
//	
//
//	/**
//	 * Create a patient store using a {@link DataSource}.
//	 * The project id must be specified with the key {@code project}. 
//	 * @param ds data source for the connection
//	 * @param configuration configuration settings
//	 * @throws SQLException SQL error
//	 */
//	public PostgresPatientStore(DataSource ds, Map<String,String> configuration) throws SQLException{
//		super(configuration);
//		this.projectId = config.get("project");
//		openDatabase(ds);
//		initialize();
//	}

	public PostgresPatientCache(){
		this.idSourceDefault = "HIVE";
		this.idSourceSeparator = ':';
		this.fetchSize = 1000;
		
	}
	public void open(Connection connection, String projectId, DataDialect dialect) throws SQLException{
		this.db = connection;
		this.projectId = projectId;
		this.dialect = dialect;
		// require project id
		Objects.requireNonNull(this.projectId, "non-null projectId required");
//		this.autoInsertSourceId = "HS.auto";
		patientCache = new Hashtable<>(1000);
		idCache = new Hashtable<>(1000);
		prepareStatements();
		loadMaxPatientNum();
		batchLoad();
	}
	private I2b2Patient getCached(int patient_num){
		return patientCache.get(patient_num);
	}
	
	public I2b2Patient lookupPatientNum(Integer patient_num){
		return getCached(patient_num);
	}

	public I2b2Patient lookupPatientId(String patient_id) {
		return getCached(patient_id);
	}

	private I2b2Patient getCached(String patient_id){
		return idCache.get(patient_id);
	}


	private void loadMaxPatientNum() throws SQLException{
		try( Statement s = db.createStatement() ){
			String sql = "SELECT MAX(patient_num) FROM patient_dimension";
			ResultSet rs = s.executeQuery(sql);
			rs.next(); // statement will always return exactly one row
			maxPatientNum = rs.getInt(1);
			if( rs.wasNull() ){
				// patient_dimension is empty
				maxPatientNum = 0;
				// numbering will start with 1
				// this is a bit redundant, since getInt()
				// will also return 0 on NULL values
			}
			rs.close();
		}
		log.info("MAX(patient_num) = "+maxPatientNum);
	}

	/**
	 * This method is called from {@link #open(Connection, String, DataDialect)}. Override to prepare additional statements.
	 * @throws SQLException SQL error
	 */
	protected void prepareStatements()throws SQLException{

		db.setAutoCommit(true);
		if( projectId == null ){
			log.warning("property project is null, some things might fail");
		}
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
		selectAll.setFetchSize(this.fetchSize);
		
		selectAllIde = db.prepareStatement("SELECT patient_num, patient_ide, patient_ide_source, patient_ide_status, project_id FROM patient_mapping WHERE project_id='"+projectId+"' ORDER BY patient_num", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		selectAllIde.setFetchSize(this.fetchSize);
		
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
	
	private void batchLoad() throws SQLException{
		try( ResultSet rs = selectAll.executeQuery() ){
			int count = 0;
			while( rs.next() ){
				count ++;
				I2b2Patient patient = loadFromResultSet(rs);
				// put in numeric patient cache
				patientCache.put(patient.getNum(), patient);
			}
			log.info("Loaded patient records: "+count);
		}
		try( ResultSet rs = selectAllIde.executeQuery() ){
			I2b2Patient p;
			ArrayList<String> ids = new ArrayList<>(16);
			// count loaded IDs for logging
			int total_count = 0, project_count=0;
			int num = -1; // current patient number
			while( rs.next() ){
				total_count ++;
				if( num == -1 ){  // first patient
					num = rs.getInt(1);
				}else if( num != rs.getInt(1) ){
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
						project_count ++;
						p.setPatientId(id);
						p.markDirty(false);
					}
				}else // all other ids are aliases
					ids.add(id);
			}
			if( num != -1 ){
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
			log.info("Loaded "+total_count+" aliases with "+project_count+" project specific IDs");
			if( project_count == 0 && total_count > 0 ){
				log.warning("No project specific patient IDs. Maybe wrong projectId?");
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
			update.setString(1, patient.getVitalStatusCd());
			update.setTimestamp(2, dialect.encodeInstantPartial(patient.getBirthDate(),patient.getSource().getSourceZone()));
			update.setTimestamp(3, dialect.encodeInstantPartial(patient.getDeathDate(),patient.getSource().getSourceZone()));
			update.setString(4, getSexCd(patient));
			if( patient.getSource().getSourceTimestamp() != null ){
				update.setTimestamp(5, dialect.encodeInstant(patient.getSource().getSourceTimestamp()));
			}else{
				update.setTimestamp(5, null);
			}
			update.setString(6, patient.getSource().getSourceId());
			update.setInt(7, patient.getNum());
			update.executeUpdate();
			patient.markDirty(false);
		}
	}


	/**
	 * Insert a new patient into the database. Only patient_num and sourcesystem_cd are filled.
	 * @param patient patient object
	 * @throws SQLException if INSERT failed
	 */
	private void insertPatient(I2b2Patient patient) throws SQLException{
		synchronized( insert ){
			insert.setInt(1, patient.getNum() );
			insert.setString(2, patient.getSource().getSourceId());
			insert.executeUpdate();
			patient.markDirty(false);
		}
	}
	
	/**
	 * Get the i2b2 sex_cd for a patient. Currently, only M and F are supported.
	 * @param patient patient object
	 * @return i2b2 sex_cd
	 */
	private static String getSexCd(Patient patient){
		if( patient.getSex() == null )return null;
		else switch( patient.getSex() ){
		case female:
			return "F";
		case male:
			return "M";
		case indeterminate:
			return "X";
		default:
			// XXX should not happen, warning
			return null;
		}
	}

	private I2b2Patient loadFromResultSet(ResultSet rs) throws SQLException{
		int id = rs.getInt(1);
		
		// load vital status code, which contains information about
		// accuracy of birth and death dates.
		String vital_cd = rs.getString(2);
		// make sure that non-null vital code contains at least one character
		if( vital_cd == null || vital_cd.length() == 0 )vital_cd = null;
		
		// birth date
		DateTimeAccuracy birthDate = dialect.decodeInstantPartial(rs.getTimestamp(3));
		DateTimeAccuracy deathDate = dialect.decodeInstantPartial(rs.getTimestamp(4));
;		
		// load sex
		String sex_cd = rs.getString(5);
		Sex sex = null;
		if( sex_cd != null ){
			switch( sex_cd.charAt(0) ){
			case 'F':
				sex = Sex.female;
				break;
			case 'M':
				sex = Sex.male;
				break;
			case 'X':
				sex = Sex.indeterminate;
				break;
			default:
				sex_cd = null; // unknown
			}
		}
		ExternalSourceImpl source = new ExternalSourceImpl();
		
		I2b2Patient patient = new I2b2Patient(id, sex, birthDate, deathDate);
		if( rs.getTimestamp(6) != null ){
			source.setSourceTimestamp(dialect.decodeInstant(rs.getTimestamp(6)));
		}
		source.setSourceId(rs.getString(7));
		// XXX right now, we cannot store the zone information.
		// use the i2b2 dialect local zone for the source zone
		source.setSourceZone(dialect.getTimeZone());
		patient.setSource(source);
		patient.setVitalStatusCd(vital_cd);
		
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
		insertIde.setTimestamp(5, dialect.encodeInstant(source.getSourceTimestamp()));
		insertIde.setString(6, source.getSourceId());
		insertIde.executeUpdate();
	}
	
	public I2b2Patient createPatient(String patientId, ExternalSourceType source){
		I2b2Patient pat = getCached(patientId);
		if( pat == null ){
			// string id not known to cache
			// create new patient
			maxPatientNum ++;
			int num = maxPatientNum;
			pat = new I2b2Patient(num, patientId);
			
			
			// don't use source metadata, since we only know the patient id
			pat.setSource(source);

			// put in cache and insert into storage
			patientCache.put(num, pat);
			idCache.put(pat.getId(), pat);
			try {
				insertPatient(pat);
				
				// insert ide into patient_mapping
				insertIde(num, pat.getId(), "A", source);
			} catch (SQLException e) {
				insertionException(pat, e);
			} 
			
			// commonly, the item is modified after a call to this method,
			// but changes are written later via a call to update.
			// (otherwise, the instance would need to know whether to perform INSERT or UPDATE)
		}
		return pat;
	}
	
//	@SuppressWarnings("unused")
//	@Override
//	public void merge(Patient patient, String additionalId, ExternalSourceType source) {
//		I2b2Patient p = (I2b2Patient)patient;
//		if( true )throw new UnsupportedOperationException();
//		// TODO add additionalId to patient.mergedIds 
//		
//		try {
//			insertIde(p.getNum(), additionalId, "M", source);
//		} catch (SQLException e) {
//			log.log(Level.SEVERE, "Unable to insert patient merge", e);
//		}
//	}

//	@Override
//	public String[] getAliasIds(Patient patient) {
//		I2b2Patient p = (I2b2Patient)patient;
//		return p.mergedIds;
//	}
//	


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
			if( p.getSource().getSourceId() != null && p.getSource().getSourceId().equals(sourceId) ){
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
		log.info("Updated "+count+" patients in database");
	}


	@Override
	public synchronized void close() throws IOException {
		if( db != null ){
			flush();
			try {
				db.close();
			} catch (SQLException e) {
				throw new IOException(e);
			}
			db = null;
		}
	}

}
