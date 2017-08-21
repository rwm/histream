package de.sekmi.histream.export;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.export.config.Concept;
import de.sekmi.histream.export.config.ExportDescriptor;
import de.sekmi.histream.export.config.ExportException;
import de.sekmi.histream.io.Streams;
import de.sekmi.histream.xml.NamespaceResolver;

/**
 * Primary class for exporting encounter/visit based
 * tables. The data to export is configured via
 * the {@link ExportDescriptor}.
 * 
 * @author R.W.Majeed
 *
 */
public class TableExport {
	private ExportDescriptor desc;
	private XPathFactory factory;
	private NamespaceContext ns;
	private ExportErrorHandler errorHandler;
	private int patientCount;
	private int visitCount;
	private ZoneId zoneId;
	
	public TableExport(ExportDescriptor desc){
		this.desc = desc;
		factory = XPathFactory.newInstance();
		ns = new NamespaceResolver();
		errorHandler = new ExportErrorHandler();
	}
	/**
	 * Specify an error handler to handle ObservationException during
	 * the {@link #export(ObservationSupplier, ExportWriter)} operation.
	 * <p>
	 * If the ObservationException is caused by an ExportException or
	 * an IOException, the cause is unwrapped and rethrown during 
	 * {@link #export(ObservationSupplier, ExportWriter)}.
	 * </p>
	 * @param handler error handler
	 */
	public void setErrorHandler(Consumer<ObservationException> handler){
		errorHandler.setErrorHandler(handler);
	}
	private XPath createXPath(){
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(ns);
		return xpath;
	}

	/**
	 * Set the zone id to use for timestamps
	 * @param zone
	 */
	public void setZoneId(ZoneId zoneId){
		this.zoneId = zoneId;
	}
	/**
	 * Make sure, that any intersection between groups are empty.
	 * Overlapping group means concepts would get multiple classes
	 * assigned, which is not supported at this time.
	 *
	 * @throws ExportException if there are overlapping concepts
	 */
	private void requireDisjointConcepts() throws ExportException{
		// easier: require unique concepts and no overlapping wildcards
		List<String> prefixes = new ArrayList<>();
		List<String> notations = new ArrayList<>();
		for( Concept concept : desc.allConcepts() ){
			String s = concept.getNotation();
			if( s != null ){
				notations.add(s);
			}
			s = concept.getWildcardNotation();
			if( s != null ){
				// wildcard must end with *
				if( s.indexOf('*') != s.length()-1 ){
					throw new ExportException("Illegal wildcard notation: "+s);
				}
				prefixes.add(s.substring(0,s.length()-1));
			}
		}
		// make sure prefixes do not overlap
		// a can overlap b only, if  a.length <= b.length
		// sort prefixes by length
		prefixes.sort( (a,b) -> a.length() - b.length() );
		for( int i=0; i<prefixes.size(); i++ ){
			String a = prefixes.get(i);
			for( int j=i+1; j<prefixes.size(); j++ ){
				String b = prefixes.get(j);
				if( b.startsWith(a) ){
					throw new ExportException("Illegal overlapping of wildcard notations '"+a+"*' and '"+b+"*");
				}
			}
		}
		// check if prefix matches any notations
		for( String prefix : prefixes ){
			for( String notation : notations ){
				if( notation.startsWith(prefix) ){
					throw new ExportException("Concepts not unique: wildcard '"+prefix+"*' matches notation '"+notation+"'");
				}
			}
		}
		// TODO check for unique notations
	}
	/**
	 * Export all observations by the given supplier to the specified {@link ExportWriter}.
	 * <p>
	 * Errors which are not {@link ExportException} and {@link IOException} (these two
	 * are usually unrecoverable) that occur during observation processing can be
	 * handled via {@link #setErrorHandler(Consumer)}.
	 * </p> 
	 * @param supplier observation supplier
	 * @param writer export writer
	 * @throws ExportException export exception
	 * @throws IOException IO exception
	 */
	public ExportSummary export(ObservationSupplier supplier, ExportWriter writer) throws ExportException, IOException{
		requireDisjointConcepts();
		FragmentExporter fe = null;
		try{
			fe = new FragmentExporter(createXPath(), desc, writer);
			fe.setZoneId(zoneId);
			fe.setErrorHandler(new ExportErrorHandler());
			Streams.transfer(supplier, fe);
		} catch (XMLStreamException | ParserConfigurationException e) {
			throw new ExportException("Unable to create exporter", e);
		} catch (UncheckedExportException e ){
			// unwrap and rethrow
			throw e.getCause();
		} catch (UncheckedIOException e ){
			// unwrap and rethrow
			throw e.getCause();
		} finally {
			// make sure all processing is completed before counting visits
			if( fe != null ){
				try{
					fe.close();
				}catch( UncheckedExportException e ){
					throw e.getCause();
				}catch( UncheckedIOException e ){
					throw e.getCause();
				}
			}
		}
		return new ExportSummary(fe.getPatientCount(), fe.getVisitCount(), fe.getObservationCount());
	}

	public int getPatientCount(){
		return patientCount;
	}
	public int getVisitCount(){
		return visitCount;
	}
}
