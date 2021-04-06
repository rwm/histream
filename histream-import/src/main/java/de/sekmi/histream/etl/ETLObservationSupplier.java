package de.sekmi.histream.etl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.etl.config.DataSource;
import de.sekmi.histream.etl.config.EavRow;
import de.sekmi.histream.etl.config.EavTable;
import de.sekmi.histream.etl.config.Meta;
import de.sekmi.histream.etl.config.PatientRow;
import de.sekmi.histream.etl.config.VisitRow;
import de.sekmi.histream.etl.config.WideRow;
import de.sekmi.histream.etl.config.WideTable;
import de.sekmi.histream.etl.filter.FilterPostProcessingQueue;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.ScopedProperty;

/**
 * Supplier for observations which are loaded from arbitrary
 * table data.
 *
 * <p>Algorithm</p>
 * <ol>
 *  <li>read first patient and first visit. -&gt; currentPatient, 
 *  currentVisit</li>
 *  
 *  <li>For each concept table (including patient and visit tables):
 *  read first row, add all concepts from first row to concept queue,
 *  sort concept queue by patid, visitid, start</li>
 * 
 *  <li>process/remove all concepts with currentPatient and 
 *  currentVisit</li>
 *  
 *  <li>if all concepts from one concept table are removed, 
 *  fetch next row from that table, add concepts to queue and sort.
 *  Go to 3.</li>
 *  
 *  <li>if no more concepts for and currentVisit
 *  are in queue, fetch next visit. Go to 3.</li>
 *  
 *  <li>if no more concepts for currentPatient are in queue,
 *  fetch next patient. Go to 3.</li>
 *  
 *  <li>if queue empty (no more patient and visit) then 
 *  done.</li>
 * </ol> 
 * <p>
 * Processing is different, if there is a script to be executed for each visit: 
 * the queue must collect all observations belonging to the same visit before 
 * executing the script, as the script might remove or add observations to the visit.
 * Therefore, the script delays observations until the script was executed.
 * </p>
 * 
 * @author marap1
 *
 */
public class ETLObservationSupplier implements ObservationSupplier{
	private RecordSupplier<PatientRow> pr;
	private RecordSupplier<VisitRow> vr;
	private List<RecordSupplier<? extends FactRow>> fr;
	
	private FactGroupingQueue queue;
	
	private DataSource ds;
	
	/**
	 * Build a new observation supplier with the supplied configuration file.
	 * Relative URLs within the configuration are resolved against the provided configuration url.
	 * 
	 * @param configuration location configuration file
	 * @param factory observation factory
	 * @return observation supplier
	 * 
	 * @throws IOException error reading configuration or data tables.
	 * @throws ParseException configuration error
	 * 
	 */
	public static ETLObservationSupplier load(URL configuration, ObservationFactory factory) throws IOException, ParseException{
		return new ETLObservationSupplier(DataSource.load(configuration), factory);
	}
		
	/**
	 * Same as {@link #load(URL, ObservationFactory)} with using a default observation factory.
	 * The default observation factory will only support Patient and Visit extensions.
	 * 
	 * @param configuration configuration URL
	 * @return observation factory
	 * @throws IOException error reading configuration or table data
	 * @throws ParseException configuration error
	 */
	public static ETLObservationSupplier load(URL configuration) throws IOException, ParseException{
		ObservationFactory of = new ObservationFactoryImpl();
		return load(configuration, of);
	}
	/** 
	 * Get the loaded configuration
	 * @return configuration
	 */
	public DataSource getConfiguration(){
		return ds;
	}
	/**
	 * Construct a new observation supplier directly from a {@link DataSource}.
	 * 
	 * @param ds data source
	 * @param factory observation factory
	 * @throws IOException error reading configuration or table data
	 * @throws ParseException configuration error
	 */
	public ETLObservationSupplier(DataSource ds, ObservationFactory factory) throws IOException, ParseException {
		this(ds,factory,new FilterPostProcessingQueue(ds, factory));
	}
	/**
	 * Construct a new observation supplier directly from a {@link DataSource}.
	 * 
	 * @param ds data source
	 * @param factory observation factory
	 * @param queue queue for collecting facts from tables
	 * @throws IOException error reading configuration or table data
	 * @throws ParseException configuration error
	 */
	protected ETLObservationSupplier(DataSource ds, ObservationFactory factory, FactGroupingQueue queue) throws IOException, ParseException {
		this.ds = ds;
		this.queue = queue;

		Meta meta = ds.getMeta();
		// in case of exception, make sure already opened suppliers are closed
		Exception error = null;
		try{
			pr = ds.getPatientTable().open(meta);
			vr = ds.getVisitTable().open(meta);

			// TODO: if there are scripts, use VisitScriptQueue
			queue.setPatientTable(pr);
			queue.setVisitTable(vr);

			// open all tables
			List<WideTable> wt = ds.getWideTables();
			fr = new ArrayList<>(wt.size());
			for( WideTable t : wt ){
				//@SuppressWarnings("resource")
				RecordSupplier<WideRow> s = t.open(meta);
				queue.addFactTable(s);
				fr.add(s);
			}
			List<EavTable> et = ds.getEavTables();
			for( EavTable t : et ){
				RecordSupplier<EavRow> s = t.open(meta);
				queue.addFactTable(s);
				fr.add(s);
			}
			queue.prepare();
			
		}catch( UncheckedIOException e ){
			error = e.getCause();
		}catch( UncheckedParseException e ){
			error = e.getCause();
		}catch( ParseException | IOException e ){
			error = e;
		}
		if( error != null ){
			try{
				this.close();
			}catch( IOException f ){
				error.addSuppressed(f);
			}
			if( error instanceof ParseException ){
				throw (ParseException)error;
			}else{
				throw (IOException)error;
			}
		}
	}
	
	@Override
	public Observation get() {
		return queue.get();
	}

	@Override
	public void close() throws IOException {
		IOException error = null;
		if( pr != null ){
			try{ pr.close(); }
			catch( IOException e ){ error = e; }
			pr=null;
		}
		if( vr != null ){
			try{ vr.close(); }
			catch( IOException e ){ 
				if( error != null )error.addSuppressed(e);
				else error = e;
			}
			vr=null;
		}
		if( fr != null ){
			Iterator<RecordSupplier<? extends FactRow>> i = fr.iterator();
			while( i.hasNext() ){
				try{ i.next().close(); }
				catch( IOException e ){ 
					if( error != null )error.addSuppressed(e);
					else error = e;
				}
				i.remove();
			}
		}

		if( error != null )throw error;
	}

	@Override
	public String getMeta(String key, String path) {
		switch( key ){
		case de.sekmi.histream.impl.Meta.META_ETL_STRATEGY:
			return ds.getMeta().getETLStrategy();
		case de.sekmi.histream.impl.Meta.META_SOURCE_ID:
			return ds.getMeta().getSourceId();
		case de.sekmi.histream.impl.Meta.META_ORDER_GROUPED:
			return "true";
		default:
			return null;
		}
	}

	@Override
	public Iterable<ScopedProperty> getMeta() {
		// TODO implement
		return Arrays.asList(
				new ScopedProperty(null,  de.sekmi.histream.impl.Meta.META_SOURCE_TIMESTAMP, Instant.ofEpochMilli(ds.getMeta().getLastModified()).toString()),
				new ScopedProperty(null,  de.sekmi.histream.impl.Meta.META_SOURCE_ID, ds.getMeta().getSourceId()),
				new ScopedProperty(null,  de.sekmi.histream.impl.Meta.META_SOURCE_TIMEZONE, ds.getMeta().getTimezone().toString())
		);
	}

}
