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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.PatientVisitStore;
import de.sekmi.histream.ext.StoredExtensionType;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.ext.Visit.Status;
import de.sekmi.histream.impl.ExternalSourceImpl;

/**
 * Visit cache which synchronizes with an i2b2 visit_dimension table.
 * Required non-null columns are encounter_num, patient_num, update_date.
 * <p>
 * Some optional columns are used: active_status_cd, start_date, end_date, inout_cd, location_cd, sourcesystem_cd
 * <p>
 * XXX after loading encounters, the String patientId not set anymore and always null. To determine the patientId, the patientStore is required for lookup of the patientNum
 * TODO use encounter_mapping table to map actual (source) patient_ide to internal patient_num for facts.
 * <p>
 * The variable argument list for {@link #createInstance(Object...)} requires the following arguments:
 * {@link String}{@code visitId}, {@link I2b2Patient}{@code patient}, {@link ExternalSourceType}{@code source}.
 * 
 * @author marap1
 *
 */
public class PostgresPatientVisitCache extends PostgresPatientCache implements PatientVisitStore, Closeable{
	private static final Logger log = Logger.getLogger(PostgresPatientVisitCache.class.getName());

	private int maxEncounterNum;
	private Hashtable<Integer, I2b2PatientVisit> visitCache;
	private Hashtable<String, I2b2PatientVisit> idCache;
	/** if true, don't allow a change of patient for a given visit. */
	private boolean rejectPatientChange;
	
	private PreparedStatement insert;
	private PreparedStatement insertMapping;
	private PreparedStatement update;
//	private PreparedStatement select;
	private PreparedStatement selectAll;
	private PreparedStatement selectMappingsAll;
	private PreparedStatement deleteSource;
	private PreparedStatement deleteMapSource;

//	/**
//	 * Create a visit store using configuration settings.
//	 * The project id must be specified with the key {@code project}. 
//	 * JDBC connection configuration is specified with the key 
//	 * prefixes {@code jdbc.*} and {@code data.jdbc.*}
//	 * @param configuration key value pairs
//	 * @throws ClassNotFoundException database driver not found
//	 * @throws SQLException SQL exceptions
//	 */
//	public PostgresVisitStore(Map<String,String> configuration) throws ClassNotFoundException, SQLException {
//		super(configuration);
//		this.projectId = config.get("project");
//		openDatabase(new String[]{"jdbc.","data.jdbc."});
//		initialize();
//	}
//
//	/**
//	 * Create a visit store using a {@link DataSource}.
//	 * The project id must be specified with the key {@code project}. 
//	 * @param ds data source for the connection
//	 * @param configuration configuration settings
//	 * @throws SQLException SQL error
//	 */
//	public PostgresVisitStore(DataSource ds, Map<String,String> configuration) throws SQLException{
//		super(configuration);
//		this.projectId = config.get("project");
//		openDatabase(ds);
//		initialize();
//	}

	public PostgresPatientVisitCache(){
		this.rejectPatientChange = false;
	}
	@Override
	public void open(Connection connection, String projectId, DataDialect dialect) throws SQLException{
		// first load patients
		super.open(connection, projectId, dialect);
		visitCache = new Hashtable<>();
		idCache = new Hashtable<>();
		loadMaxEncounterNum();
		batchLoad(); /// XXX loading visits does not set the String patientId, for that, the patientStore would be needed
	}
	@Override
	protected void prepareStatements() throws SQLException {
		super.prepareStatements();
		// TODO: use prefix from configuration to specify tablespace
		insert = db.prepareStatement("INSERT INTO visit_dimension(encounter_num, patient_num, import_date, download_date, sourcesystem_cd) VALUES(?,?,current_timestamp,?,?)");
		insertMapping = db.prepareStatement("INSERT INTO encounter_mapping(encounter_num, encounter_ide, encounter_ide_source, patient_ide, patient_ide_source, encounter_ide_status, project_id, import_date, download_date, sourcesystem_cd) VALUES(?,?,?,?,?,'A','"+projectId+"',current_timestamp,?,?)");
		update = db.prepareStatement("UPDATE visit_dimension SET patient_num=?, active_status_cd=?, start_date=?, end_date=?, inout_cd=?, location_cd=?, update_date=current_timestamp, download_date=?, sourcesystem_cd=? WHERE encounter_num=?");
		//select = db.prepareStatement("SELECT encounter_num, patient_num, active_status_cd, start_date, end_date, inout_cd, location_cd, update_date, sourcesystem_cd FROM visit_dimension WHERE patient_num=?");
		selectAll = db.prepareStatement("SELECT encounter_num, patient_num, active_status_cd, start_date, end_date, inout_cd, location_cd, download_date, sourcesystem_cd FROM visit_dimension", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		selectAll.setFetchSize(fetchSize);
		selectMappingsAll = db.prepareStatement("SELECT encounter_num, encounter_ide, encounter_ide_source, patient_ide, patient_ide_source, encounter_ide_status, project_id FROM encounter_mapping ORDER BY encounter_num", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		selectMappingsAll.setFetchSize(fetchSize);

		deleteSource = db.prepareStatement("DELETE FROM visit_dimension WHERE sourcesystem_cd=?");
		deleteMapSource = db.prepareStatement("DELETE FROM encounter_mapping WHERE sourcesystem_cd=?");		
	}

	public int size(){
		return visitCache.size();
	}

	public void setRejectPatientChange(boolean rejectPatientChange){
		this.rejectPatientChange = rejectPatientChange;
	}

	private void loadMaxEncounterNum() throws SQLException{
		try( Statement s = db.createStatement() ){
			String sql = "SELECT MAX(encounter_num) FROM visit_dimension";
			ResultSet rs = s.executeQuery(sql);
			if( rs.next() ){
				maxEncounterNum = rs.getInt(1);
			}else{
				// patient_dimension is empty
				// start numbering patients with 1
				maxEncounterNum = 1;
			}
			rs.close();
		}
		log.info("MAX(encounter_num) = "+maxEncounterNum);
	}

	public I2b2PatientVisit lookupEncounterNum(Integer encounter_num){
		return visitCache.get(encounter_num);
	}
	public void loadMaxInstanceNums() throws SQLException{
		// TODO maybe better to load only encounters+max instance_num for current project -> join with encounter_mapping
		
		log.info("Loading maximum instance_num for each encounter");
		Statement stmt = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		final String sql = "SELECT patient_num, encounter_num, MAX(instance_num) FROM observation_fact GROUP BY patient_num, encounter_num";
		int count = 0;
		int noMatch = 0;
		try( ResultSet rs = stmt.executeQuery(sql) ){
			while( rs.next() ){
				I2b2PatientVisit v = visitCache.get(rs.getInt(2));
				if( v != null )v.maxInstanceNum = rs.getInt(3);
				else noMatch ++;
				count ++;
			}
		}
		stmt.close();
		log.info("Loaded MAX(instance_num) for "+count+" encounters");
		if( noMatch != 0 ){
			log.warning("Encountered "+noMatch+" encounter_num in observation_fact without matching visits");
		}
	}
	
	private String pasteId(String source, String ide){
		if( source == null || source.equals(idSourceDefault) )return ide;
		else return source+":"+ide;
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
	
	/**
	 * Set aliases for a visit, puts aliases into cache.
	 * 
	 * @param visit visit instance
	 * @param aliases alias patient IDs (e.g. merged)
	 * @param primary index of primary alias (in aliases)
	 */
	private void setAliases(I2b2PatientVisit visit, String[] aliases, int primary){
		visit.setAliases(aliases, primary);
		// put in cache
		for( String id : aliases ){
			idCache.put(id, visit);
		}
	}
	private void batchSetAliases(int num, ArrayList<String> aliases, int primary){
		I2b2PatientVisit visit = visitCache.get(num);
		if( visit == null ){
			log.warning("Missing row in visit_dimension for encounter_mapping.encounter_num="+num);
		}else{
			setAliases(visit, aliases.toArray(new String[aliases.size()]), primary);
			visit.markDirty(false);
		}
	}

	private void batchLoad() throws SQLException{
		// load visits
		try( ResultSet rs = selectAll.executeQuery() ){
			while( rs.next() ){
				I2b2PatientVisit visit = loadFromResultSet(rs);
				visitCache.put(visit.getNum(), visit);
			}
		}

		// load id mappings
		try( ResultSet rs = selectMappingsAll.executeQuery() ){
			int num = -1; // current patient number
			ArrayList<String> ids = new ArrayList<>(16);
			int primary=0; // primary alias index
			
			while( rs.next() ){
				if( num == -1 )num = rs.getInt(1);
				else if( num != rs.getInt(1) ){
					// next encounter mapping
					batchSetAliases(num, ids, primary);
					// XXX
					ids.clear();
					num = rs.getInt(1);
					primary = 0;
				}
				String id = pasteId(rs.getString(3), rs.getString(2));
				if( rs.getString(6).equals("A") && rs.getString(7).equals(projectId) ){
					// active id for project
					primary = ids.size();
					// TODO maybe use any other Active encounter as primary, if the project doesn't match
				}
				ids.add(id);
			}

			if( num != -1 ){
				// don't forget last encounter
				batchSetAliases(num, ids, primary);
			}
		}
	}
	/*
	private I2b2PatientVisit retrieveFromStorage(String id) throws IOException{
		synchronized( select ){
			try {
				select.setInt(1, Integer.parseInt(id));
			} catch (NumberFormatException | SQLException e) {
				throw new IOException(e);
			}
			try( ResultSet rs = select.executeQuery() ){
				if( !rs.next() )return null;
				return loadFromResultSet(rs);
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}
	}*/
	
	private void updateStorage(I2b2PatientVisit visit) throws SQLException {
		synchronized( update ){
			update.setInt(1, visit.getPatientNum());
			update.setString(2, visit.getActiveStatusCd());
			update.setTimestamp(3, dialect.encodeInstantPartial(visit.getStartTime(),visit.getSource().getSourceZone()));
			update.setTimestamp(4, dialect.encodeInstantPartial(visit.getEndTime(),visit.getSource().getSourceZone()));
			update.setString(5, visit.getInOutCd());
			update.setString(6, dialect.encodeLocationCd(visit.getLocationId()));
			update.setTimestamp(7, dialect.encodeInstant(visit.getSource().getSourceTimestamp()));
			update.setString(8, visit.getSource().getSourceId());

			// where encounter_num=visit.getNum()
			update.setInt(9, visit.getNum());
			int rows = update.executeUpdate();
			if( rows == 0 ){
				log.warning("UPDATE executed for visit_dimension.encounter_num="+visit.getNum()+", but no rows changed.");
			}
			// clear dirty flag
			visit.markDirty(false);
		}
	}



	/**
	 * Add the visit to storage. Patient information is not written
	 * @param visit visit to add
	 * @throws SQLException if the INSERT failed
	 */
	private void addToStorage(I2b2PatientVisit visit) throws SQLException{
		synchronized( insert ){
			insert.setInt(1, visit.getNum() );
			insert.setInt(2, visit.getPatientNum());
			insert.setTimestamp(3, dialect.encodeInstant(visit.getSource().getSourceTimestamp()));
			insert.setString(4, visit.getSource().getSourceId());
			insert.executeUpdate();
			// other fields are not written, don't clear the dirty flag
		}
		synchronized( insertMapping ){
			insertMapping.setInt(1, visit.getNum());
			String[] ids = splitId(visit.getId());
			insertMapping.setString(2, ids[1]); // encounter_ide
			insertMapping.setString(3, ids[0]); // encounter_ide_source
			
			// XXX warning, this is only safe if PostgresPatientStore and PostgresVisitStore use same idSourceSeparator and idSourceDefault
			ids = splitId(visit.getPatientId()); // TODO better solution
			insertMapping.setString(4, ids[1]); // patient_ide
			insertMapping.setString(5, ids[0]); // patient_ide_source
			
			
			insertMapping.setTimestamp(6, dialect.encodeInstant(visit.getSource().getSourceTimestamp()));
			insertMapping.setString(7, visit.getSource().getSourceId());
			insertMapping.executeUpdate();
		}
	}
	


	private I2b2PatientVisit loadFromResultSet(ResultSet rs) throws SQLException{
		int id = rs.getInt(1);
		int patid = rs.getInt(2);
		// XXX String patientId is always null after loading from the database.

		// load vital status code, which contains information about
		// accuracy of birth and death dates.
		String active_status_cd = rs.getString(3);
		// make sure that non-null vital code contains at least one character
		if( active_status_cd != null && active_status_cd.length() == 0 )active_status_cd = null;
		
		DateTimeAccuracy startDate = dialect.decodeInstantPartial(rs.getTimestamp(4));
		DateTimeAccuracy endDate = dialect.decodeInstantPartial(rs.getTimestamp(5));
		
		// load sex
		String inout_cd = rs.getString(6);
		Status status = null;
		if( inout_cd != null ){
			switch( inout_cd.charAt(0) ){
			case 'I':
				status = Status.Inpatient;
				break;
			case 'O':
				status = Status.Outpatient;
				break;
			}
		}
		
		// TODO: use patid
		I2b2PatientVisit visit = new I2b2PatientVisit(id, patid);
		visit.setStartTime(startDate);
		visit.setEndTime(endDate);
		visit.setStatus(status);
		visit.setActiveStatusCd(active_status_cd);

		visit.setLocationId(dialect.decodeLocationCd(rs.getString(7)));
		ExternalSourceImpl source = new ExternalSourceImpl();
		source.setSourceTimestamp(dialect.decodeInstant(rs.getTimestamp(8)));
		source.setSourceId(rs.getString(9));
		source.setSourceZone(dialect.getTimeZone());
		visit.setSource(source);
		// additional fields go here
		
		// assign patient
		I2b2Patient patient = lookupPatientNum(patid);
		if( patient == null ) {
			// visit in database with illegal patient reference. patient does not exist
			// call warning handler
			// TODO decide what to do.. create patient? fail loading visit? or maybe create a temporary memory patient without storage
			log.severe("Visit "+id+" references non-existing patient_num "+patid);
		}else {
			visit.setPatient(patient);
		}
		// mark clean
		visit.markDirty(false);
		return visit;
	}
	/*
	private void retrievalException(String id, IOException e) {
		log.log(Level.SEVERE, "Unable to retrieve visit "+id, e);
	}
 	*/
	private void insertionException(I2b2PatientVisit visit, SQLException e) {
		log.log(Level.SEVERE, "Unable to insert visit "+visit.getId(), e);
	}

	private void updateException(I2b2PatientVisit visit, SQLException e) {
		log.log(Level.SEVERE, "Unable to update visit "+visit.getId(), e);
	}

	@Override
	public Visit createVisit(String visitId, DateTimeAccuracy start, Patient patient, ExternalSourceType source) {
		if( !(patient instanceof I2b2Patient) ) {
			throw new IllegalArgumentException("Patient argument must be of type I2b2Patient");
		}
		return this.createVisit(visitId, start, (I2b2Patient)patient, source);
		
	}
	public I2b2PatientVisit createVisit(String encounterId, I2b2Patient patient, ExternalSourceType source){
		I2b2PatientVisit visit = idCache.get(encounterId);
		
		if( visit == null ){
			// visit does not exist, create a new one

			maxEncounterNum ++;
			int encounter_num = maxEncounterNum;
			visit = new I2b2PatientVisit(encounter_num, patient.getNum());
			visit.setPatient(patient);
			
			// created from observation, use source metadata
			visit.setSource(source);

			// put in cache
			visitCache.put(encounter_num, visit);
			

			// only one alias which is also primary
			setAliases(visit, new String[]{encounterId}, 0);
			
			// insert to storage
			try {
				addToStorage(visit);
			} catch (SQLException e) {
				insertionException(visit, e);
			} // put in cache and insert into storage
			// commonly, the item is modified after a call to this method,
			// but changes are written later via a call to update.
			// (otherwise, the instance would need to know whether to perform INSERT or UPDATE)
		}else {
			// visit already existing
			// verify that the patient number from the visit matches with the observation
			if( visit.getPatientNum() != patient.getNum() ){
				// throw exception to abort processing
				if( rejectPatientChange ){
					throw new AssertionError("Patient_num mismatch for visit "+encounterId+": history says "+visit.getPatientNum()+" while data says "+patient.getNum(), null);
				}else {
					log.info("Updating visit #"+visit.getNum()+" for patient change from #"+visit.getPatientNum()+" to #"+patient.getNum());
					visit.setPatient(patient);
				}
			}
		}
		return visit;		
	}

	/**
	 * Find a visit. Does not create the visit if it doesn't exist.
	 * @param id visit id/alias
	 * @return visit or {@code null} if not found.
	 */
	public I2b2PatientVisit findVisit(String id){
		return idCache.get(id);
	}
	
	@Override
	public void deleteWhereSourceId(String sourceId) throws SQLException {
		// first delete patient
		super.deleteWhereSourceId(sourceId);
		// then visit
		deleteSource.setString(1, sourceId);
		int numRows = deleteSource.executeUpdate();
		log.info("Deleted "+numRows+" rows with sourcesystem_cd = "+sourceId);

		deleteMapSource.setString(1, sourceId);
		deleteMapSource.executeUpdate();
		// find matching patients in cache
		Enumeration<I2b2PatientVisit> all = visitCache.elements();
		LinkedList<I2b2PatientVisit>remove = new LinkedList<>();
		while( all.hasMoreElements() ){
			I2b2PatientVisit p = all.nextElement();
			if( p.getSource().getSourceId() != null && p.getSource().getSourceId().equals(sourceId) ){
				remove.add(p); // remove later, otherwise the Enumeration might fail
			}
			// XXX does not work with sourceId == null
		}
		// remove patients from cache
		for( I2b2PatientVisit p : remove ){
			visitCache.remove(p.getNum());
			
			for( String id : p.aliasIds ){
				idCache.remove(id);
			}
		}
		// XXX some ids might remain in patient_mapping, because we don't store the patient_mapping sourcesystem_cd
		// usually this should work, as we assume sourcesystem_cd to be equal for patients in both tables
		
		// reload MAX(patient_num)
		loadMaxEncounterNum();
	}

	@Override
	public void flush() {
		// flush patients first
		super.flush();
		// now visits
		Iterator<I2b2PatientVisit> dirty = StoredExtensionType.dirtyIterator(visitCache.elements());
		int count = 0;
		while( dirty.hasNext() ){
			I2b2PatientVisit visit = dirty.next();
			try {
				updateStorage(visit);
				count ++;
			} catch (SQLException e) {
				updateException(visit, e);
			}
		}
		if( count != 0 )log.info("Updated "+count+" visits in database");
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
	@Override
	public Patient findPatient(String patientId) {
		return lookupPatientId(patientId);
	}
	@Override
	public void merge(Patient patient, String additionalId, ExternalSourceType source) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getPatientAliasIds(Patient patient) {
		throw new UnsupportedOperationException();
	}
	@Override
	public void purgePatient(String patientId) {
		throw new UnsupportedOperationException();
	}
	@Override
	public void purgeVisit(String visitId) {
		throw new UnsupportedOperationException();
	}
	@Override
	public List<? extends Visit> allVisits(Patient patient) {
		throw new UnsupportedOperationException();
	}
}
