package de.sekmi.histream.i2b2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Logger;

import javax.sql.DataSource;

import de.sekmi.histream.ObservationFactory;

/**
 * Extract observations from i2b2.
 * <p>
 * Allows simple queries against the i2b2 observation_fact table
 * and retrieval of facts.
 * 
 * @author R.W.Majeed
 *
 */
public class I2b2ExtractorFactory implements AutoCloseable {
	private static final Logger log = Logger.getLogger(I2b2ExtractorFactory.class.getName());

	private DataSource ds;
	private Integer fetchSize;
	DataDialect dialect;
	private ObservationFactory observationFactory;
	
	private static String SELECT_PARAMETERS = "patient_num, encounter_num, instance_num, concept_cd, modifier_cd, provider_id, location_cd, start_date, end_date, RTRIM(valtype_cd) valtype_cd, tval_char, nval_num, RTRIM(valueflag_cd) valueflag_cd, units_cd, sourcesystem_cd";
	private static String SELECT_TABLE = "observation_fact";
	//private static String SELECT_ORDER_CHRONO = "ORDER BY start_date, patient_num, encounter_num, instance_num, modifier_cd NULLS FIRST";
	private static String SELECT_ORDER_GROUP = "ORDER BY patient_num, encounter_num, start_date, instance_num, concept_cd, modifier_cd NULLS FIRST";

	public I2b2ExtractorFactory(DataSource crc_ds, ObservationFactory factory) throws SQLException{
		// TODO implement
		this.observationFactory = factory;
		ds = crc_ds;
		dialect = new DataDialect();
	}
	public ObservationFactory getObservationFactory(){
		return observationFactory;
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
	/**
	 * Extract observations for given concept codes with 
	 * {@code observation.start} between start_min and start_end.
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
			b.append("WHERE start_date BETWEEN ? AND ? ");
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
