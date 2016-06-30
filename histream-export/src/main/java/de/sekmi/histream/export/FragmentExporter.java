package de.sekmi.histream.export;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import de.sekmi.histream.ObservationException;
import de.sekmi.histream.export.config.ExportDescriptor;
import de.sekmi.histream.export.config.ExportException;

public class FragmentExporter extends VisitFragmentParser {

	TableParser patientParser;
	TableParser visitParser;
	
	protected FragmentExporter(XPath xpath, ExportDescriptor desc, ExportWriter writer) throws ExportException, XMLStreamException, ParserConfigurationException {
		super();
		
		patientParser = desc.getPatientTable().createParser(writer.openPatientTable(), xpath);
		visitParser = desc.getVisitTable().createParser(writer.openVisitTable(), xpath);
	}

	@Override
	protected void patientFragment(Node patient) throws ObservationException {
		try {
			patientParser.writeRow(patient);
		} catch (ExportException e) {
			throw new ObservationException(e);
		}
	}

	@Override
	protected void visitFragment(Node visit) throws ObservationException {
		try {
			visitParser.writeRow(visit);
		} catch (ExportException e) {
			throw new ObservationException(e);
		}
	}

	@Override
	public void close(){
		super.close();
		try{
			patientParser.close();
		}catch( IOException e ){
			reportError(new ObservationException(e));
		}
		try{
			visitParser.close();
		}catch( IOException e ){
			reportError(new ObservationException(e));
		}
	}
}
