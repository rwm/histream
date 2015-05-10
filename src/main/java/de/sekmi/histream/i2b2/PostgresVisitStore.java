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
import de.sekmi.histream.ext.StoredExtensionType;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.ext.Visit.Status;

/**
 * Visit cache which synchronizes with an i2b2 visit_dimension table.
 * Required non-null columns are encounter_num, patient_num, update_date.
 * <p>
 * Some optional columns are used: active_status_cd, start_date, end_date, inout_cd, location_cd, sourcesystem_cd
 * <p>
 * TODO use encounter_mapping table to map actual (source) patient_ide to internal patient_num for facts.
 * 
 * @author marap1
 *
 */
public class PostgresVisitStore extends PostgresExtension<I2b2Visit>{
	private static final Logger log = Logger.getLogger(PostgresVisitStore.class.getName());
	private static final Class<?>[] INSTANCE_TYPES = new Class<?>[]{Visit.class,I2b2Visit.class};
	
	private String projectId;
	private int maxEncounterNum;
	private char idSourceSeparator;
	private String idSourceDefault;
	
	//private static ChronoUnit[] map_date_units = {ChronoUnit.DAYS, ChronoUnit.MONTHS, ChronoUnit.YEARS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS};
	//private static char[] map_death_chars = {};
	private Hashtable<Integer, I2b2Visit> visitCache;
	private Hashtable<String, I2b2Visit> idCache;
	
	private PreparedStatement insert;
	private PreparedStatement insertMapping;
	private PreparedStatement update;
//	private PreparedStatement select;
	private PreparedStatement selectAll;
	private PreparedStatement selectMappingsAll;
	private PreparedStatement deleteSource;
	private PreparedStatement deleteMapSource;
	
	public PostgresVisitStore(Map<String,String> configuration) {
		super(configuration);
		visitCache = new Hashtable<>();
		idCache = new Hashtable<>();
		projectId = config.get("project");
		idSourceDefault = "HIVE";
		idSourceSeparator = ':';
	}
	
	@Override
	public void open()throws SQLException, ClassNotFoundException{
		super.open();
		// db is already opened by super.connect()
		db.setAutoCommit(true);
		// TODO: use prefix from configuration to specify tablespace
		insert = db.prepareStatement("INSERT INTO visit_dimension(encounter_num, patient_num, import_date, download_date, sourcesystem_cd) VALUES(?,?,current_timestamp,?,?)");
		insertMapping = db.prepareStatement("INSERT INTO encounter_mapping(encounter_num, encounter_ide, encounter_ide_source, patient_ide, patient_ide_source, encounter_ide_status, project_id, import_date, download_date, sourcesystem_cd) VALUES(?,?,?,?,?,'A','"+projectId+"',current_timestamp,?,?)");
		update = db.prepareStatement("UPDATE visit_dimension SET active_status_cd=?, start_date=?, end_date=?, inout_cd=?, location_cd=?, update_date=current_timestamp, download_date=?, sourcesystem_cd=? WHERE encounter_num=?");
		//select = db.prepareStatement("SELECT encounter_num, patient_num, active_status_cd, start_date, end_date, inout_cd, location_cd, update_date, sourcesystem_cd FROM visit_dimension WHERE patient_num=?");
		selectAll = db.prepareStatement("SELECT encounter_num, patient_num, active_status_cd, start_date, end_date, inout_cd, location_cd, download_date, sourcesystem_cd FROM visit_dimension", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		selectAll.setFetchSize(getFetchSize());
		selectMappingsAll = db.prepareStatement("SELECT encounter_num, encounter_ide, encounter_ide_source, patient_ide, patient_ide_source, encounter_ide_status, project_id FROM encounter_mapping ORDER BY encounter_num", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		selectMappingsAll.setFetchSize(getFetchSize());

		deleteSource = db.prepareStatement("DELETE FROM visit_dimension WHERE sourcesystem_cd=?");
		deleteMapSource = db.prepareStatement("DELETE FROM encounter_mapping WHERE sourcesystem_cd=?");

		loadMaxEncounterNum();
		batchLoad();
	}
	
	public int size(){
		return visitCache.size();
	}
	
	private void loadMaxEncounterNum() throws SQLException{
		Statement s = db.createStatement();
		String sql = "SELECT MAX(encounter_num) FROM visit_dimension";
		try( ResultSet rs = s.executeQuery(sql) ){
			if( rs.next() ){
				maxEncounterNum = rs.getInt(1);
			}else{
				// patient_dimension is empty
				// start numbering patients with 1
				maxEncounterNum = 1;
			}
		}
		log.info("MAX(encounter_num) = "+maxEncounterNum);
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
				I2b2Visit v = visitCache.get(rs.getInt(2));
				if( v != null )v.maxInstanceNum = rs.getInt(3);
				else noMatch ++;
				count ++;
			}
		}
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
	 * @param visit
	 * @param aliases
	 * @param primary
	 */
	private void setAliases(I2b2Visit visit, String[] aliases, int primary){
		visit.aliasIds = aliases;
		visit.primaryAliasIndex = primary;
		visit.setId(aliases[primary]);
		// put in cache
		for( String id : aliases ){
			idCache.put(id, visit);
		}
	}
	private void batchSetAliases(int num, ArrayList<String> aliases, int primary){
		I2b2Visit visit = visitCache.get(num);
		if( visit == null ){
			log.warning("Missing row in visit_dimension for encounter_mapping.encounter_num="+num);
		}else{
			setAliases(visit, aliases.toArray(new String[aliases.size()]), primary);
		}
	}
	
	private void batchLoad() throws SQLException{
		// load visits
		try( ResultSet rs = selectAll.executeQuery() ){
			while( rs.next() ){
				I2b2Visit visit = loadFromResultSet(rs);
				visitCache.put(visit.getNum(), visit);
			}
		}

		// load id mappings
		try( ResultSet rs = selectMappingsAll.executeQuery() ){
			int num = 0; // current patient number
			ArrayList<String> ids = new ArrayList<>(16);
			int primary=0; // primary alias index
			
			while( rs.next() ){
				if( num == 0 )num = rs.getInt(1);
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
			// don't forget last encounter
			batchSetAliases(num, ids, primary);
		}
	}
	/*
	private I2b2Visit retrieveFromStorage(String id) throws IOException{
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
	
	private void updateStorage(I2b2Visit visit) throws SQLException {
		synchronized( update ){
			update.setString(1, getActiveStatusCd(visit));
			update.setTimestamp(2, inaccurateSqlTimestamp(visit.getStartTime()));
			update.setTimestamp(3, inaccurateSqlTimestamp(visit.getEndTime()));
			update.setString(4, getInOutCd(visit));
			update.setString(5, visit.getLocationId());
			update.setTimestamp(6, Timestamp.from(visit.getSourceTimestamp()));
			update.setString(7, visit.getSourceId());

			// where encounter_num=visit.getNum()
			update.setInt(8, visit.getNum());
			int rows = update.executeUpdate();
			if( rows == 0 ){
				log.warning("UPDATE executed for visit_dimension.encounter_num="+visit.getNum()+", but no rows changed.");
			}
			// clear dirty flag
			visit.markDirty(false);
		}
	}

	private static String getInOutCd(Visit patient){
		if( patient.getStatus() == null )return null;
		else switch( patient.getStatus() ){
		case Inpatient:
			return "I";
		case Outpatient:
		case Emergency: // unsupported by i2b2, map to outpatient
			return "O";
		default:
			// XXX should not happen, warning
			return null;
		}
	}


	/**
	 * Add the visit to storage. Patient information is not written
	 * @param visit visit to add
	 * @throws SQLException
	 */
	private void addToStorage(I2b2Visit visit) throws SQLException{
		synchronized( insert ){
			insert.setInt(1, visit.getNum() );
			insert.setInt(2, visit.getPatientNum());
			insert.setTimestamp(3, Timestamp.from(visit.getSourceTimestamp()));
			insert.setString(4, visit.getSourceId());
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
			
			
			insertMapping.setTimestamp(6, Timestamp.from(visit.getSourceTimestamp()));
			insertMapping.setString(7, visit.getSourceId());
			insertMapping.executeUpdate();
		}
	}
	
	/**
	 * Get the i2b2 vital_status_cd for a patient
	 * @param visit
	 * @return vital status code, see CRC_Design doc
	 */
	private static String getActiveStatusCd(Visit visit){
		char end_char=0, start_char=0;
		if( visit.getEndTime() != null ){
			switch( visit.getEndTime().getAccuracy() ){
			case DAYS:
				end_char = 'Y';
				break;
			case MONTHS:
				end_char = 'M';
				break;
			case YEARS:
				end_char = 'X';
				break;
			case HOURS:
				end_char = 'R';
				break;
			case MINUTES:
				end_char = 'T';
				break;
			case SECONDS:
				end_char = 'S';
				break;
			default:
			}
		}else{
			// null end date
			// TODO: U: unknown, O: ongoing
		}

		// birth date
		if( visit.getStartTime() != null ){
			switch( visit.getStartTime().getAccuracy() ){
			case DAYS:
				end_char = 'D';
				end_char = 0; // same meaning
				break;
			case MONTHS:
				end_char = 'B';
				break;
			case YEARS:
				end_char = 'F';
				break;
			case HOURS:
				end_char = 'H';
				break;
			case MINUTES:
				end_char = 'I';
				break;
			case SECONDS:
				end_char = 'C';
				break;
			default:
			}
		}else{
			// null start date
			// TODO: L: unknown, A: active
		}

		if( end_char != 0 && start_char != 0 )
			return new String(new char[]{end_char,start_char});
		else if( end_char != 0 )
			return new String(new char[]{end_char});
		else if( start_char != 0 )
			return new String(new char[]{start_char});
		else return null;
	}

	private void setActiveStatusCd(Visit patient, String vital_cd){
		// load accuracy
		if( vital_cd == null )return; // nothing to do
		
		ChronoUnit accuracy = null;
		char birthIndicator = 0;
		
		// end date indicator
		switch( vital_cd.charAt(0) ){
		case 'U': // unknown, no date
		case 'O': // ongoing, no date
			// TODO
			break;
		case 0:
		case 'Y': // known, accurate to day
			accuracy = ChronoUnit.DAYS;
			break;
		case 'M': // known, accurate to month
			accuracy = ChronoUnit.MONTHS;
			break;
		case 'X': // known, accurate to year
			accuracy = ChronoUnit.YEARS;
			break;
		case 'R': // known, accurate to hour
			accuracy = ChronoUnit.HOURS;
			break;
		case 'T': // known, accurate to minute
			accuracy = ChronoUnit.MINUTES;
			break;
		case 'S': // known, accurate to second
			accuracy = ChronoUnit.SECONDS;
			break;
		default:
			// no match for end date -> check for start status in first character
			birthIndicator = vital_cd.charAt(0);
		}

		if( patient.getEndTime() != null && accuracy != null ){
			patient.getEndTime().setAccuracy(accuracy);
		}
		if( birthIndicator == 0 && vital_cd.length() > 1 )
			birthIndicator = vital_cd.charAt(1);
		// birth date indicator
		switch( birthIndicator ){
		case 'L': // unknown, no date
		case 'A': // active, no date
			// TODO
			break;
		case 0: // same as D
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
		if( patient.getStartTime() != null && accuracy != null ){
			patient.getStartTime().setAccuracy(accuracy);
		}		
	}
	private I2b2Visit loadFromResultSet(ResultSet rs) throws SQLException{
		int id = rs.getInt(1);
		int patid = rs.getInt(2);
		
		// load vital status code, which contains information about
		// accuracy of birth and death dates.
		String active_status_cd = rs.getString(3);
		// make sure that non-null vital code contains at least one character
		if( active_status_cd != null && active_status_cd.length() == 0 )active_status_cd = null;
		
		DateTimeAccuracy startDate = null;
		DateTimeAccuracy endDate = null;
		// birth date
		Timestamp ts = rs.getTimestamp(4);
		if( ts != null ){
			startDate = new DateTimeAccuracy(ts.toLocalDateTime());
		}
		// death date
		ts = rs.getTimestamp(5);
		if( ts != null ){
			endDate = new DateTimeAccuracy(ts.toLocalDateTime());
		}
		
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
		I2b2Visit visit = new I2b2Visit(id, patid);
		visit.setStartTime(startDate);
		visit.setEndTime(endDate);
		visit.setStatus(status);
		setActiveStatusCd(visit, active_status_cd);

		visit.setLocationId(rs.getString(7));
		visit.setSourceTimestamp(rs.getTimestamp(8).toInstant());
		visit.setSourceId(rs.getString(9));
		
		// additional fields go here
		
		// mark clean
		visit.markDirty(false);
		return visit;
	}
	/*
	private void retrievalException(String id, IOException e) {
		log.log(Level.SEVERE, "Unable to retrieve visit "+id, e);
	}
 	*/
	private void insertionException(I2b2Visit visit, SQLException e) {
		log.log(Level.SEVERE, "Unable to insert visit "+visit.getId(), e);
	}

	private void updateException(I2b2Visit visit, SQLException e) {
		log.log(Level.SEVERE, "Unable to update visit "+visit.getId(), e);
	}

	@Override
	public I2b2Visit createInstance(Observation fact) {
		// TODO create visit using fact.getPatientId() and fact.getVisitId()
		I2b2Visit visit = idCache.get(fact.getEncounterId());
		
		if( visit == null ){
			maxEncounterNum ++;
			int encounter_num = maxEncounterNum;
			I2b2Patient p = fact.getExtension(I2b2Patient.class);
			visit = new I2b2Visit(encounter_num, p.getNum());
			visit.setPatientId(fact.getPatientId());
			
			// created from observation, use source metadata
			visit.setSourceId(fact.getSourceId());
			visit.setSourceTimestamp(fact.getSourceTimestamp());

			// put in cache
			visitCache.put(encounter_num, visit);
			

			// only one alias which is also primary
			setAliases(visit, new String[]{fact.getEncounterId()}, 0);
			
			// insert to storage
			try {
				addToStorage(visit);
			} catch (SQLException e) {
				insertionException(visit, e);
			} // put in cache and insert into storage
			// commonly, the item is modified after a call to this method,
			// but changes are written later via a call to update.
			// (otherwise, the instance would need to know whether to perform INSERT or UPDATE)
		}
		return visit;
	}

	@Override
	public Class<?>[] getInstanceTypes() {
		return INSTANCE_TYPES;
	}


	// TODO unify method with PostgresVisitStore
	@Override
	public void deleteWhereSourceId(String sourceId) throws SQLException {
		deleteSource.setString(1, sourceId);
		int numRows = deleteSource.executeUpdate();
		log.info("Deleted "+numRows+" rows with sourcesystem_cd = "+sourceId);

		deleteMapSource.setString(1, sourceId);
		deleteMapSource.executeUpdate();
		// find matching patients in cache
		Enumeration<I2b2Visit> all = visitCache.elements();
		LinkedList<I2b2Visit>remove = new LinkedList<>();
		while( all.hasMoreElements() ){
			I2b2Visit p = all.nextElement();
			if( p.getSourceId() != null && p.getSourceId().equals(sourceId) ){
				remove.add(p); // remove later, otherwise the Enumeration might fail
			}
			// XXX does not work with sourceId == null
		}
		// remove patients from cache
		for( I2b2Visit p : remove ){
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
	protected void prepareStatements() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public I2b2Visit createInstance() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void flush() {
		Iterator<I2b2Visit> dirty = StoredExtensionType.dirtyIterator(visitCache.elements());
		int count = 0;
		while( dirty.hasNext() ){
			I2b2Visit visit = dirty.next();
			try {
				updateStorage(visit);
				count ++;
			} catch (SQLException e) {
				updateException(visit, e);
			}
		}
		if( count != 0 )log.info("Updated "+count+" visits in database");
	}

}
