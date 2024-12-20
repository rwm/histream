package de.sekmi.histream.i2b2;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.sekmi.histream.AbnormalFlag;
import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.Value;
import de.sekmi.histream.impl.ExternalSourceImpl;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.ObservationImpl;
import de.sekmi.histream.impl.ScopedProperty;
import de.sekmi.histream.impl.StringValue;
import de.sekmi.histream.impl.VisitPatientImpl;

/**
 * Retrieves observations from i2b2. See {@link I2b2ExtractorFactory}.
 * @author R.W.Majeed
 *
 */
public abstract class I2b2Extractor implements ObservationSupplier {
	private static final Logger log = Logger.getLogger(I2b2Extractor.class.getName());

	protected I2b2ExtractorFactory factory;
	protected Connection dbc;

	/** ResultSet with rows from {@code observation_fact} table. 
	 * Initially positioned before the first row. It is accessed read only
	 * and forward only. Its columns are required to be in the following order:
	 * <ol>
	 *  <li>patient_num/id (int or String)</li>
	 *  <li>encounter_num/id (int or String)</li>
	 *  <li>instance_num</li>
	 *  <li>concept_cd</li>
	 *  <li>modifier_cd</li>
	 *  <li>provider_id</li>
	 *  <li>location_cd</li>
	 *  <li>start_date</li>
	 *  <li>end_date</li>
	 *  <li>RTRIM(valtype_cd) valtype_cd, tval_char, nval_num, RTRIM(valueflag_cd) valueflag_cd, units_cd, sourcesystem_cd</li>
	 * </ol>
	 **/
	private PreparedStatement ps;
	private ResultSet rs;
	private boolean finished;
	
	/**
	 * Constructs a new extractor. Connection and ResultSet need to be 
	 * closed by a call to {@link #close()} if the object is not needed
	 * anymore.
	 * <p>
	 * 	
	 * </p>
	 * @param factory extractor factory
	 * @param dbc database connection
	 * @throws SQLException error
	 */
	I2b2Extractor(I2b2ExtractorFactory factory, Connection dbc) throws SQLException {
		this.factory = factory;
		this.dbc = dbc;
	}

	protected abstract PreparedStatement prepareQuery() throws SQLException;

	/**
	 * Prepares and executes the query, producing the result set
	 * which can then be used to fetch observations.
	 * <p>
	 * This method can be called manually before the first call to {@link #get()}.
	 * If not called manually, it will be called automatically during the first
	 * call to {@link #get()}. During that implicit invocations, any errors are
	 * forwarded to the {@link #errorHandler(SQLException)}.
	 * </p>
	 * @throws SQLException SQL error
	 */
	public void prepareResultSet() throws SQLException{
		if( rs != null ){
			// this method assumes that there is no previous result set
			throw new IllegalStateException();
		}
		this.ps = prepareQuery();
		this.rs = ps.executeQuery();
		if( rs.next() == false ){
			// empty result set, no observations to process.
			finished = true;
		}		
	}
	/**
	 * Retrieves errors during the get() operation.
	 * The default implementation is to wrap the exception
	 * in an unchecked exception which is then thrown.
	 * <p>
	 * TODO document unchecked exception
	 * </p>
	 * @param exception exception
	 */
	protected void errorHandler(SQLException exception){
		throw new UncheckedSQLException(exception);
	}
	
	private static class Row{
		int pid;
		int eid;
		Integer inst;
		/** concept id */
		String cid;
		/** modifier id */
		String mid;
		String lid;
		Timestamp start;
		Timestamp end;
		Timestamp source_ts;
		String source_cd;
		String vt;
		String vc;
		BigDecimal vn;
		AbnormalFlag vf;
		String vu;
	}
	private Row loadRow() throws SQLException{
		Row row = new Row();
		row.pid = rs.getInt(1); // patient num
		row.eid = rs.getInt(2); // encounter num
		row.inst = rs.getInt(3);
		if( rs.wasNull() ){
			row.inst = null;
		}
		row.cid = rs.getString(4); // concept id
		row.mid = factory.dialect.decodeModifierCd(rs.getString(5)); // modifier id
		// provider id 6
		row.lid = factory.dialect.decodeLocationCd(rs.getString(7)); // location id
		row.start = rs.getTimestamp(8);
		row.end = rs.getTimestamp(9);
		// value
		row.vt = factory.dialect.decodeValueTypeCd(rs.getString(10));
		row.vc = rs.getString(11);
		row.vn = rs.getBigDecimal(12);
		row.vf = factory.dialect.decodeValueFlagCd(rs.getString(13));
		row.vu = factory.dialect.decodeUnitCd(rs.getString(14));
	
		// need source
		row.source_ts = rs.getTimestamp(15);
		row.source_cd = rs.getString(16);
		return row;
	}
	private Value createValue(Row row){
		if( row.vt == null ){
			return null; // no value
		}else if( row.vt.equals("T") ){
			StringValue v = new StringValue(row.vc);
			v.setAbnormalFlag(row.vf);
			return v;
		}else if( row.vt.equals("N") ){
			NumericValue v = new NumericValue(row.vn, row.vu);
			v.setAbnormalFlag(row.vf);
			return v;
		}else{
			log.severe("Ignoring unsupported value type '"+row.vt+"' for concept "+row.cid);
			return null;
		}
	}
	private Observation createObservation(Row row){
		// map/lookup patient_num -> Patient, encounter_num -> Visit
//		Patient patient = null;
//		String patientId = null;
//		if( factory.lookupPatientNum != null ){
//			patient = factory.lookupPatientNum.apply(row.pid);
//			if( patient == null ){
//				log.severe("Unable to find patient with patient_num="+row.pid);
//			}
//		}
//		if( patient != null ){
//			patientId = patient.getId();
//		}else{
//			patientId = Integer.toString(row.pid);
//		}
	
		// parse visit
		VisitPatientImpl visit = null;
		if( factory.lookupVisitNum != null ){
			visit = factory.lookupVisitNum.apply(row.eid);
			if( visit == null ){
				log.severe("Unable to find visit with encounter_num="+row.eid);
			}
		}
//		Observation o = factory.getObservationFactory().createObservation(visit, row.cid, new DateTimeAccuracy(row.start.toInstant()));
		Observation o = ObservationImpl.createObservation(visit, row.cid, new DateTimeAccuracy(row.start.toInstant()));
		if( row.end != null ){
			o.setEndTime(new DateTimeAccuracy(row.end.toInstant()));
		}
		o.setValue(createValue(row));
		if( row.lid != null ){
			o.setLocationId(row.lid);
		}
		// TODO try to cache external source if same as previous
		o.setSource(new ExternalSourceImpl(row.source_cd, row.source_ts.toInstant()));
		// TODO more properties
		return o;
	}
	private boolean isModifier(Row fact, Row modifier){
		return( fact.pid == modifier.pid
				&& fact.eid == modifier.eid
				&& fact.inst != null // not needed for i2b2, but other tables (e.g.HIStream) may allow NULL instance num)
				&& modifier.inst != null
				&& fact.inst.equals(modifier.inst)
				&& modifier.mid != null );
	}
	@Override
	public Observation get() {
		if( finished == true ){
			return null;
		}
		if( rs == null ){
			try {
				prepareResultSet();
			} catch (SQLException e) {
				finished = true; // prevent repeating the query execution failure
				// if we fail here, we are unable retrieve any fact
				errorHandler(e);
				return null;
			}
		}
		Observation o = null;
		try{
			// next/first observation is always top concept
			Row r = loadRow();
			// validate row. modifier_cd should be null
			if( r.mid != null ){
				throw new SQLException("Null modifier expected for first fact in group");
			}
			o = createObservation(r);
			// load modifiers
			for(;;){
				if( rs.next() == false ){
					// last row
					finished = true;
					// observation complete
					break;
				}
				Row m = loadRow();
				if( isModifier(r, m) ){
					// TODO validate modifier, start should be equal to fact
					o.addModifier(m.mid, createValue(m));
				}else{
					// no modifier for main fact
					// fact is finished
					break;
				}
			}
		}catch( SQLException e ){
			errorHandler(e);
		}
		return o;
	}

	@Override
	public String getMeta(String key, String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<ScopedProperty> getMeta() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public void close() {
		log.info("Closing extractor "+this.toString());
		try{
			if( rs != null )rs.close();
			if( ps != null )ps.close();
		}catch( SQLException e){
			log.log(Level.WARNING,"Failed to close RecordSet",e);
		}
		try{
			dbc.close();
		}catch( SQLException e){
			log.log(Level.WARNING,"Failed to close connection",e);
		}
	}
	
	public void dump() throws SQLException{
		int count = 0;
		if( finished ){
			return;
		}
		do{
			count ++;
			StringBuilder b = new StringBuilder(200);
			b.append("row(");
			b.append(count);
			b.append("): ");
			b.append(rs.getInt(1));
			b.append(", ");
			b.append(rs.getInt(2));
			b.append(", ");
			b.append(rs.getInt(3));
			b.append(", ");
			b.append(rs.getString(4));
			b.append(", ");
			b.append(rs.getString(5));
			b.append(", tval=");
			b.append(rs.getString(12));
			System.out.println(b.toString());
		}while( rs.next() );
		finished = true;
		System.out.println("Count:"+count);
	}

}
