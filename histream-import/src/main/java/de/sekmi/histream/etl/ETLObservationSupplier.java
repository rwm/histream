package de.sekmi.histream.etl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXB;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.etl.config.DataSource;
import de.sekmi.histream.etl.config.Meta;
import de.sekmi.histream.etl.config.PatientTable;
import de.sekmi.histream.etl.config.VisitTable;
import de.sekmi.histream.etl.config.WideTable;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.SimplePatientExtension;
import de.sekmi.histream.impl.SimpleVisitExtension;

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
 *  
 * 
 * @author marap1
 *
 */
public class ETLObservationSupplier implements ObservationSupplier{
	
	private PatientTable pt;
	private VisitTable vt;
	private List<WideTable> wt;
	
	private RecordSupplier<PatientRow> pr;
	private RecordSupplier<VisitRow> vr;
	private List<RecordSupplier<WideRow>> wr;
	
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
	 * @throws IOException error reading configuration. The error might be caused by a {@link ParseException}.
	 * 
	 */
	public static ETLObservationSupplier load(URL configuration, ObservationFactory factory) throws IOException{
		DataSource ds = JAXB.unmarshal(configuration, DataSource.class);
		ds.getMeta().setLocation(configuration);
		return new ETLObservationSupplier(ds, factory);
	}
	
	/**
	 * Same as {@link #load(URL, ObservationFactory)} with using a default observation factory.
	 * The default observation factory will only support Patient and Visit extensions.
	 * 
	 * @param configuration configuration URL
	 * @return observation factory
	 * @throws IOException error reading configuration
	 */
	public static ETLObservationSupplier load(URL configuration) throws IOException{
		ObservationFactory of = new ObservationFactoryImpl();
		of.registerExtension(new SimplePatientExtension());
		of.registerExtension(new SimpleVisitExtension());
		return load(configuration, of);
	}
	/**
	 * Construct a new observation supplier directly from a {@link DataSource}.
	 * 
	 * @param ds data source
	 * @param factory observation factory
	 * @throws IOException error reading configuration
	 */
	public ETLObservationSupplier(DataSource ds, ObservationFactory factory) throws IOException {
		this.ds = ds;
		
		pt = ds.getPatientTable();
		vt = ds.getVisitTable();
		wt = ds.getWideTables();
		// TODO long tables

		Meta meta = ds.getMeta();
		// in case of exception, make sure already opened suppliers are closed
		IOException error = null;
		try{
			pr = pt.open(factory, meta);
			vr = vt.open(factory, meta);
			queue = new FactGroupingQueue(pr, vr, 
					factory.getExtensionAccessor(Patient.class), 
					factory.getExtensionAccessor(Visit.class));

			// open all tables
			wr = new ArrayList<>(wt.size());
			for( WideTable t : wt ){
				@SuppressWarnings("resource")
				RecordSupplier<WideRow> s = t.open(factory, meta);
				queue.addFactTable(s);
				wr.add(s);
			}
			queue.prepare();
			
		}catch( UncheckedIOException e ){
			error = e.getCause();
		}catch( UncheckedParseException e ){
			error = new IOException(e.getCause());
		}catch( ParseException e ){
			error = new IOException(e);
		}catch( IOException e ){
			error = e;
		}
		if( error != null ){
			try{
				this.close();
			}catch( IOException f ){
				error.addSuppressed(f);
			}
			throw error;
		}
	}
	
	@Override
	public Observation get() {
		return queue.next();
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
		if( wr != null ){
			Iterator<RecordSupplier<WideRow>> i = wr.iterator();
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
	public String getMeta(String key) {
		switch( key ){
		case ObservationSupplier.META_ETL_STRATEGY:
			return ds.getMeta().getETLStrategy();
		case ObservationSupplier.META_SOURCE_ID:
			return ds.getMeta().getSourceId();
		case ObservationSupplier.META_ORDER_GROUPED:
			return "true";
		default:
			return null;
		}
	}

}
