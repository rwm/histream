package de.sekmi.histream.i2b2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Logger;

import javax.sql.DataSource;

import de.sekmi.histream.ObservationFactory;

/**
 * Extract observations from i2b2.
 * <p>
 * Allows simple queries against the i2b2 observation_fact table
 * and retrieval of facts.
 * </p>
 * TODO add/use interface from histream-core
 * @author R.W.Majeed
 *
 */
public class I2b2ExtractorFactory implements AutoCloseable {
	private static final Logger log = Logger.getLogger(I2b2ExtractorFactory.class.getName());

	private DataSource ds;
	private Integer fetchSize;
	DataDialect dialect;
	private ObservationFactory observationFactory;
	private boolean allowWildcardConceptCodes;
	
	/**
	 * Boolean feature whether to allow wildcard concept keys. 
	 * <p>
	 * Use with caution: Unexpected results might happen if wildcard 
	 * concepts overlap. (Such as query fails, duplicate facts, etc.)
	 * </p>
	 */
	public static String ALLOW_WILDCARD_CONCEPT_CODES = "de.sekmi.histream.i2b2.wildcard_concepts";
	
	
	private static String SELECT_PARAMETERS = "f.patient_num, f.encounter_num, f.instance_num, f.concept_cd, f.modifier_cd, f.provider_id, f.location_cd, f.start_date, f.end_date, RTRIM(f.valtype_cd) valtype_cd, f.tval_char, f.nval_num, RTRIM(f.valueflag_cd) valueflag_cd, f.units_cd, f.sourcesystem_cd";
	private static String SELECT_TABLE = "observation_fact f";
	//private static String SELECT_ORDER_CHRONO = "ORDER BY start_date, patient_num, encounter_num, instance_num, modifier_cd NULLS FIRST";
	private static String SELECT_ORDER_GROUP = "ORDER BY f.patient_num, f.encounter_num, f.start_date, f.instance_num, f.concept_cd, f.modifier_cd NULLS FIRST";

	public I2b2ExtractorFactory(DataSource crc_ds, ObservationFactory factory) throws SQLException{
		// TODO implement
		this.observationFactory = factory;
		ds = crc_ds;
		dialect = new DataDialect();
	}
	public ObservationFactory getObservationFactory(){
		return observationFactory;
	}
	
	public void setFeature(String feature, Object value){
		if( feature.equals(ALLOW_WILDCARD_CONCEPT_CODES) ){
			if( value instanceof Boolean ){
				this.allowWildcardConceptCodes = (Boolean)value;
			}else{
				throw new IllegalArgumentException("Boolean value expected for feature "+feature);
			}
		}
	}
	public PreparedStatement prepareStatement(Connection dbc, String sql) throws SQLException{
		PreparedStatement s = dbc.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if( fetchSize != null ){
			s.setFetchSize(fetchSize);
			s.setFetchDirection(ResultSet.FETCH_FORWARD);
		}
		return s;
	}

	public void setProperty(String property, Object value){
		// de.sekmi.histream.i2b2.extractor.project
	}
	
	private void createTemporaryConceptTable(Connection dbc, String[] concepts) throws SQLException{
		try( Statement s = dbc.createStatement() ){
			s.executeUpdate("CREATE TEMPORARY TABLE temp_concepts(concept VARCHAR(255) PRIMARY KEY)");			
		}
		try( PreparedStatement ps 
				= dbc.prepareStatement("INSERT INTO temp_concepts(concept) VALUES(?)") ){
			for( int i=0; i<concepts.length; i++ ){
				ps.clearParameters();
				ps.clearWarnings();
				ps.setString(1, concepts[i]);
				ps.executeUpdate();
			}
		}
		
	}
	
	private String escapeLikeString(String likeString){
		// TODO escape _ and % with backslash
		return likeString;
	}
	/**
	 * Extract observations for given concept codes with 
	 * {@code observation.start} between start_min and start_end.
	 * <p>
	 * The query can use the index {@code of_idx_clusteredconcept} for {@code concept_cd}
	 * and {@code of_idx_start_date} for {@code start_date}.
	 * </p>
	 * <p>
	 * TODO integration test without concepts, with normal concepts, 
	 * with wildcard concepts, wildcard concepts with literal _ or % in id
	 * </p>
	 * 
	 * @param start_min start date of returned observations must be greater than start_min
	 * @param start_max start date of returned observations must be less than start_max
	 * @param concepts concept ids to extract
	 * @return extractor
	 * @throws SQLException error
	 */
	//@SuppressWarnings("resource")
	public I2b2Extractor extract(Timestamp start_min, Timestamp start_max, String[] concepts) throws SQLException{
		// TODO move connection and prepared statement to I2b2Extractor
		Connection dbc = ds.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			dbc.setAutoCommit(false);
			StringBuilder b = new StringBuilder(600);
			b.append("SELECT ");
			b.append(SELECT_PARAMETERS+" FROM "+SELECT_TABLE+" ");
			if( concepts != null ){
				log.info("Temporary table created for "+concepts.length+" concept ids");
				String[] ids = concepts;
				int wildcardCount = 0;
				if( allowWildcardConceptCodes ){
					// TODO check if wildcards actually used (search for *)
					ids = new String[concepts.length];
					for( int i=0; i<ids.length; i++ ){
						ids[i] = escapeLikeString(concepts[i]).replace('*', '%');
						if( false == ids[i].equals(concepts[i]) ){
							wildcardCount ++;
						}
					}
					// TODO add check for overlapping wildcard concepts (e.g. A* and AB*)
				}
				createTemporaryConceptTable(dbc, ids);
				if( wildcardCount > 0 ){
					b.append(" JOIN temp_concepts tc ON f.concept_cd LIKE tc.concept ");					
				}else{
					b.append(" JOIN temp_concepts tc ON f.concept_cd=tc.concept ");
				}
			}
			b.append("WHERE f.start_date BETWEEN ? AND ? ");
			b.append(SELECT_ORDER_GROUP);
			log.info("SQL: "+b.toString());
	
			ps = prepareStatement(dbc, b.toString());
			ps.setTimestamp(1, start_min);
			ps.setTimestamp(2, start_max);
			rs = ps.executeQuery();
			return new I2b2Extractor(this, dbc, rs);
		}catch( SQLException e ){
			// clean up
			if( rs != null ){
				rs.close();
			}
			if( ps != null ){
				ps.close();
			}
			dbc.close();
			throw e;
		}
	}
	
	@Override
	public void close() throws SQLException {

	}
}
