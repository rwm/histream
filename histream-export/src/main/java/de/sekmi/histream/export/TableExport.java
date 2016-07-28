package de.sekmi.histream.export;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationSupplier;
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
	public void export(ObservationSupplier supplier, ExportWriter writer) throws ExportException, IOException{
		try( FragmentExporter e = new FragmentExporter(createXPath(), desc, writer) ){
			e.setErrorHandler(new ExportErrorHandler());
			Streams.transfer(supplier, e);
		} catch (XMLStreamException | ParserConfigurationException e) {
			throw new ExportException("Unable to create exporter", e);
		} catch (UncheckedExportException e ){
			// unwrap and rethrow
			throw e.getCause();
		} catch (UncheckedIOException e ){
			// unwrap and rethrow
			throw e.getCause();
		}
	}
}
