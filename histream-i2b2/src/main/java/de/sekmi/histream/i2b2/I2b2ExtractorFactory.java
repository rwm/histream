package de.sekmi.histream.i2b2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.sql.DataSource;


import de.sekmi.histream.ObservationExtractor;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

/**
 * Extract observations from i2b2.
 * <p>
 * Allows simple queries against the i2b2 observation_fact table
 * and retrieval of facts.
 * </p>
 * TODO add/use interface from histream-core
 * XXX TODO allow to map patient_num -&gt; Patient and encounter_num -&gt; Encounter, this must be done before the extension is accessed
 * @author R.W.Majeed
 *
 */
public class I2b2ExtractorFactory implements AutoCloseable, ObservationExtractor {
	private static final Logger log = Logger.getLogger(I2b2ExtractorFactory.class.getName());

	private DataSource ds;
	private Integer fetchSize;
	private ObservationFactory observationFactory;

	DataDialect dialect;
	boolean allowWildcardConceptCodes;
	boolean useEncounterTiming;
	
	Function<Integer,? extends Patient> lookupPatientNum;
	Function<Integer,? extends Visit> lookupVisitNum;
	/**
	 * Boolean feature whether to allow wildcard concept keys. 
	 * <p>
	 * Use with caution: Unexpected results might happen if wildcard 
	 * concepts overlap. (Such as query fails, duplicate facts, etc.)
	 * </p>
	 */
	public static final String ALLOW_WILDCARD_CONCEPT_CODES = "de.sekmi.histream.i2b2.wildcard_concepts";
	public static final String USE_ENCOUNTER_TIMESTAMPS = "de.sekmi.histream.i2b2.encounter_timing";
	
	

	public I2b2ExtractorFactory(DataSource crc_ds, ObservationFactory factory) throws SQLException{
		// TODO implement
		this.observationFactory = factory;
		ds = crc_ds;
		dialect = new DataDialect();
		fetchSize = 500;
	}

	public ObservationFactory getObservationFactory(){
		return observationFactory;
	}
	
	public void setPatientLookup(Function<Integer, ? extends Patient> lookup){
		this.lookupPatientNum = lookup;		
	}
	public void setVisitLookup(Function<Integer, ? extends Visit> lookup){
		this.lookupVisitNum = lookup;
	}
	public void setFeature(String feature, Object value){
		switch( feature ){
		case ALLOW_WILDCARD_CONCEPT_CODES:
			if( value instanceof Boolean ){
				this.allowWildcardConceptCodes = (Boolean)value;
			}else{
				throw new IllegalArgumentException("Boolean value expected for feature "+feature);
			}
			break;
		case USE_ENCOUNTER_TIMESTAMPS:
			if( value instanceof Boolean ){
				this.useEncounterTiming = (Boolean)value;
			}else{
				throw new IllegalArgumentException("Boolean value expected for feature "+feature);
			}
			break;
		default:
			throw new IllegalArgumentException("Feature not supported:"+feature+"="+value);
		}
	}

	PreparedStatement prepareStatementForLargeResultSet(Connection dbc, String sql) throws SQLException{
		PreparedStatement s = dbc.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if( fetchSize != null ){
			s.setFetchSize(fetchSize);
			s.setFetchDirection(ResultSet.FETCH_FORWARD);
		}
		return s;
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
	 * @param notations concept ids to extract
	 * @return extractor
	 * @throws SQLException error
	 */
	//@SuppressWarnings("resource")
	I2b2Extractor extract(Timestamp start_min, Timestamp start_max, Iterable<String> notations) throws SQLException{
		Connection dbc = null;
		try{ // no try with resource, because we need to pass the connection to the extractor
			dbc = ds.getConnection();
			dbc.setAutoCommit(true);
			I2b2ExtractorImpl ei = new I2b2ExtractorImpl(this, dbc);
			ei.setInterval(start_min, start_max);
			ei.setNotations(notations);
			ei.prepareResultSet();
			return ei;
		}catch( SQLException e ){
			// clean up
			if( dbc != null ){
				dbc.close();
			}
			throw e;
		}
	}
	
	@Override
	public void close() {

	}
	@Override
	public I2b2Extractor extract(Instant start_min, Instant start_max, Iterable<String> notations) throws IOException{
		try {
			return extract(dialect.encodeInstant(start_min),dialect.encodeInstant(start_max), notations);
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	private int[] fetchEncounterNums(Iterable<Visit> visits){
		List<Visit> vl;
		if( visits instanceof List ){
			vl = (List<Visit>)visits;
		}else{
			vl = new ArrayList<>();
			visits.forEach(vl::add);
		}
		int[] nums = new int[vl.size()];
		Iterator<Visit> vi = vl.iterator();
		for( int i=0; i<nums.length; i++ ){
			Visit v = vi.next();
			Objects.requireNonNull(v, "null visit in argument list");
			int num;
			if( v instanceof I2b2Visit ){
				num = ((I2b2Visit) v).getNum();
			}else{
				throw new IllegalStateException("encounter_num not available for visit type "+v.getClass());
			}
			nums[i] = num;
		}
		return nums;
	}
	@Override
	public I2b2Extractor extract(Iterable<Visit> visits, Iterable<String> notations) throws IOException {
		Connection dbc = null;
		try{ // no try with resource, because we need to pass the connection to the extractor
			dbc = ds.getConnection();
			dbc.setAutoCommit(true);
			I2b2ExtractorImpl ei = new I2b2ExtractorImpl(this, dbc);
			ei.setVisits(fetchEncounterNums(visits));
			ei.setNotations(notations);
			ei.prepareResultSet();
			return ei;
		}catch( SQLException e ){
			// clean up
			if( dbc != null ){
				try {
					dbc.close();
				} catch (SQLException e1) {
					e.addSuppressed(e1);
				}
			}
			throw new IOException(e);
		}
	}
}
