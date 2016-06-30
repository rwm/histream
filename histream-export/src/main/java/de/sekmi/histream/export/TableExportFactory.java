package de.sekmi.histream.export;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.export.config.ExportDescriptor;
import de.sekmi.histream.export.config.ExportException;
import de.sekmi.histream.io.Streams;
import de.sekmi.histream.xml.NamespaceResolver;

public class TableExportFactory {
	private ExportDescriptor desc;
	private XPathFactory factory;
	private NamespaceContext ns;
	
	public TableExportFactory(ExportDescriptor desc){
		this.desc = desc;
		factory = XPathFactory.newInstance();
		ns = new NamespaceResolver();
	}
	
	private XPath createXPath(){
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(ns);
		return xpath;
	}
	public void export(ObservationSupplier supplier, ExportWriter writer) throws ExportException{
		try( FragmentExporter e = new FragmentExporter(createXPath(), desc, writer) ){
			Streams.transfer(supplier, e);
		} catch (XMLStreamException | ParserConfigurationException e) {
			throw new ExportException("Unable to create exporter", e);
		}
	}
}
