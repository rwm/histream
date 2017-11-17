package de.sekmi.histream.i2b2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import de.sekmi.histream.ext.Visit;

public class I2b2ExtractorImpl extends I2b2Extractor {
	private static final Logger log = Logger.getLogger(I2b2ExtractorImpl.class.getName());
	private static String SELECT_PARAMETERS = "f.patient_num, f.encounter_num, f.instance_num, f.concept_cd, f.modifier_cd, f.provider_id, f.location_cd, f.start_date, f.end_date, RTRIM(f.valtype_cd) valtype_cd, f.tval_char, f.nval_num, RTRIM(f.valueflag_cd) valueflag_cd, f.units_cd, f.download_date, f.sourcesystem_cd";
	private static String SELECT_TABLE = "observation_fact f";
	//private static String SELECT_ORDER_CHRONO = "ORDER BY start_date, patient_num, encounter_num, instance_num, modifier_cd NULLS FIRST";
	private static String SELECT_ORDER_GROUP = "ORDER BY f.patient_num, f.encounter_num, f.start_date, f.instance_num, f.concept_cd, f.modifier_cd NULLS FIRST";

	private Iterable<String> notations;
	private int[] encounter_nums;
	
	private Timestamp interval_start;
	private Timestamp interval_end;
	private String temporaryTableSuffix;
	private List<String> temporaryTables;
	private List<Object> joinArguments;
	private List<Object> whereArguments;
	private int maxInlineArgs;

	I2b2ExtractorImpl(I2b2ExtractorFactory factory, Connection dbc) throws SQLException {
		super(factory, dbc);
		temporaryTables = new ArrayList<>();
		joinArguments = new ArrayList<>();
		whereArguments = new ArrayList<>();
	}

	private String tempTableName(String base){
		return base+temporaryTableSuffix;
	}
	private void limitNotations(List<String> joinParts, List<String> whereParts) throws SQLException{
		if( notations == null ){
			// no limit, return everything
			return;
		}
		// check if there is a limited number of notations
		log.info("Creating temporary table for concept ids");
		Iterable<String> ids = notations;
		int wildcardCount = 0;
		if( factory.allowWildcardConceptCodes ){
			List<String>escaped = new ArrayList<>();
			for( String id : ids ){
				String es = escapeLikeString(id).replace('*', '%');
				// check if wildcards actually used
				if( false == es.equals(id) ){
					wildcardCount ++;
				}
				escaped.add(es);
			}
			ids = escaped;
			// TODO add check for overlapping wildcard concepts (e.g. A* and AB*)
		}
		createTemporaryConceptTable(dbc, ids);
		if( wildcardCount > 0 ){
			joinParts.add("INNER JOIN "+tempTableName("temp_concepts")+" tc ON f.concept_cd LIKE tc.concept");				
		}else{
			joinParts.add("INNER JOIN "+tempTableName("temp_concepts")+" tc ON f.concept_cd=tc.concept");
		}

	}

	private void limitTimeframe(List<String> joinParts, List<String> whereParts){
		if( interval_start == null && interval_end == null ){
			// nothing to do
			return;
		}
		Objects.requireNonNull(interval_start);
		Objects.requireNonNull(interval_end);		
		if( factory.useEncounterTiming ){
			// interval refers to encounter start.
			// build and join an encounter table
			joinParts.add("INNER JOIN encounter_dimension ed ON ed.encouner_num=f.encounter_num AND ed.start_time BETWEEN ? AND ?");
			joinArguments.add(interval_start);
			joinArguments.add(interval_end);
		}else{
			// interval refers to fact start
			whereParts.add("f.start_time BETWEEN ? AND ?");
			whereArguments.add(interval_start);
			whereArguments.add(interval_end);
		}
	}

	/**
	 * Build a String like {@code (?,?,?,?)} to use with an SQL IN statement.
	 * @param length number of question marks in the list
	 * @return string containing the list
	 */
	private static String generateInlineIn(int length){
		StringBuilder b = new StringBuilder(length*2+2);
		b.append('(');
		for( int i=0; i<length; i++ ){
			if( i != 0 ){
				b.append(',');
			}
			b.append('?');
		}
		b.append(')');
		return b.toString();
	}
	private void limitVisits(List<String> joinParts, List<String> whereParts){
		if( encounter_nums == null ){
			// no restriction, nothing to do
			return;
		}
		if( encounter_nums.length == 1 ){
			// single visit, use inline sql
			whereParts.add("f.encounter_num = ?");
			whereArguments.add(Integer.valueOf(encounter_nums[0]));
		}else if( encounter_nums.length < maxInlineArgs ){
			whereParts.add("f.encounter_num IN "+generateInlineIn(encounter_nums.length));
			for( int i=0; i<encounter_nums.length; i++ ){
				whereArguments.add(Integer.valueOf(encounter_nums[i]));
			}
		}else{
			// TODO implement temporary visit table
			throw new UnsupportedOperationException("Temporary visit table not implemented yet for specified visits");
		}
	}
	@Override
	protected PreparedStatement prepareQuery() throws SQLException {
		StringBuilder b = new StringBuilder(600);
		b.append("SELECT ");
		b.append(SELECT_PARAMETERS+" FROM "+SELECT_TABLE+" ");

		List<String> joinParts = new ArrayList<>();
		List<String> whereParts = new ArrayList<>();
		
		limitNotations(joinParts, whereParts);

		limitTimeframe(joinParts, whereParts);

		limitVisits(joinParts, whereParts);

		// JOIN ...
		for( String part : joinParts ){
			b.append(part).append(' ');
		}
		// WHERE ...
		if( !whereParts.isEmpty() ){
			b.append("WHERE ");
			boolean first = true;
			for( String part : whereParts ){
				if( first == true ){
					first = false;
				}else{
					b.append(" AND ");
				}
				b.append(part);
			}
		}
		// GROUP ...
		b.append(SELECT_ORDER_GROUP);

		PreparedStatement ps = factory.prepareStatementForLargeResultSet(dbc, b.toString());
		int arg = 1;
		for( Object o : joinArguments ){
			ps.setObject(arg, o);
			arg ++;
		}
		for( Object o : whereArguments ){
			ps.setObject(arg, o);
			arg ++;			
		}
		// TODO debug log query and arguments
		return ps;
		
//		StringBuilder b = createSelect(dbc, notations);
//		b.append("WHERE f.start_date BETWEEN ? AND ? ");
//		log.info("SQL: "+b.toString());
//
//		ps.setTimestamp(1, start_min);
//		ps.setTimestamp(2, start_max);
//		rs = ps.executeQuery();
//		return new I2b2Extractor(this, dbc, rs);

	}


	/**
	 * Limit by specifying encounter_num's to use
	 * @param visits 
	 */
	public void setVisits(int[] encounter_num){
		this.encounter_nums = encounter_num;
	}
	public void setNotations(Iterable<String> notations){
		// TODO any need for preprocessing notations?
		this.notations = notations;
	}
	public void setInterval(Timestamp start, Timestamp end){
		interval_start = start;
		interval_end = end;
	}

	/**
	 * Create a temporary visit table with the specified visits
	 * @param dbc connection
	 * @param visits visits
	 * @throws SQLException SQL errors
	 */
	private void createTemporaryVisitTable(Connection dbc, Iterable<Visit> visits) throws SQLException{
		// delete table if previously existing
		try( Statement s = dbc.createStatement() ){
			s.executeUpdate("DROP TABLE IF EXISTS temp_visits");
		}
		try( Statement s = dbc.createStatement() ){
			s.executeUpdate("CREATE TEMPORARY TABLE temp_visits(encounter_num INTEGER PRIMARY KEY)");			
		}
		try( PreparedStatement ps 
				= dbc.prepareStatement("INSERT INTO temp_visits(encounter_num) VALUES(?)") ){
			for( Visit visit : visits ){
				ps.clearParameters();
				ps.clearWarnings();
				int vn;
				if( visit instanceof I2b2Visit ){
					vn = ((I2b2Visit)visit).getNum();
				}else{
					throw new SQLException("Unsupported visit type "+visit.getClass());
				}
				ps.setInt(1, vn);
				ps.executeUpdate();
			}
		}
	}
	private void createTemporaryConceptTable(Connection dbc, Iterable<String> concepts) throws SQLException{
		// delete table if previously existing
		try( Statement s = dbc.createStatement() ){
			s.executeUpdate("DROP TABLE IF EXISTS "+tempTableName("temp_concepts"));
		}
		try( Statement s = dbc.createStatement() ){
			s.executeUpdate("CREATE TEMPORARY TABLE "+tempTableName("temp_concepts")+"(concept VARCHAR(255) PRIMARY KEY)");
			temporaryTables.add("temp_concepts");
		}
		try( PreparedStatement ps 
				= dbc.prepareStatement("INSERT INTO "+tempTableName("temp_concepts")+"(concept) VALUES(?)") ){
			// TODO do we need to make sure that there are no duplicate concepts???
			for( String concept : concepts ){
				ps.clearParameters();
				ps.clearWarnings();
				ps.setString(1, concept);
				ps.executeUpdate();
			}
		}
		
	}
	
	private String escapeLikeString(String likeString){
		// TODO escape _ and % with backslash
		return likeString;
	}

}
