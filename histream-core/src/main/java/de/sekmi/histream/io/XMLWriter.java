package de.sekmi.histream.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.Plugin;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.AbstractObservationHandler;
import de.sekmi.histream.impl.ObservationImpl;

/**
 * Writes observations to a single XML file. Observations must be grouped by patient and encounter.
 * TODO write test case which reads+writes XML file and compares input+output literally.
 * TODO prevent duplicate namespace declarations by JAXB.marshal of facts
 * 
 * @author Raphael
 *
 */
public class XMLWriter extends AbstractObservationHandler implements Closeable, Plugin {
	private static final String NAMESPACE = "http://sekmi.de/histream/ns/eav-data";
	private Patient prevPatient;
	private Visit prevVisit;
	private Marshaller marshaller;
	private OutputStream output;
	private XMLStreamWriter writer;
	private boolean writeFormatted;
	private int formattingDepth;
	
	public XMLWriter(Map<String,String> config) throws JAXBException, XMLStreamException, FactoryConfigurationError{
		this.marshaller = JAXBContext.newInstance(ObservationImpl.class).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		this.writeFormatted = true;
		this.output = System.out;
		XMLOutputFactory factory = XMLOutputFactory.newFactory();

		this.writer = factory.createXMLStreamWriter(output, "UTF-8");
		writer.writeStartDocument();
		formatNewline();
		writer.setDefaultNamespace(NAMESPACE);
		//?? writer.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		writer.writeStartElement(NAMESPACE, JAXBObservationSupplier.DOCUMENT_ROOT);
		writer.writeDefaultNamespace(NAMESPACE);
		writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		formatNewline();
		formatPush();
		//Transformer transformer = TransformerFactory.newInstance().newTransformer();
		//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//transformer.
	}
	@Override
	public void setMeta(String key, String value) {
		// TODO Auto-generated method stub

	}
	private void formatNewline() throws XMLStreamException{
		if( writeFormatted )writer.writeCharacters("\n");
	}
	private void formatIndent() throws XMLStreamException{
		if( writeFormatted )for( int i=0; i<formattingDepth; i++ ){
			writer.writeCharacters("\t");
		}
	}
	private void formatPush(){
		formattingDepth ++;
	}
	private void formatPop(){
		formattingDepth --;
	}

	private void startEncounter(Visit visit)throws XMLStreamException{
		formatIndent();
		writer.writeStartElement(JAXBObservationSupplier.ENCOUNTER_ELEMENT);
		writer.writeAttribute("id", visit.getId());
		formatNewline();
		formatPush();
		
		if( visit.getStartTime() != null ){
			formatIndent();
			writer.writeStartElement("start");
			writer.writeCharacters(visit.getStartTime().toPartialIso8601());
			writer.writeEndElement();
			formatNewline();
		}
		if( visit.getEndTime() != null ){
			formatIndent();
			writer.writeStartElement("end");
			writer.writeCharacters(visit.getEndTime().toPartialIso8601());
			writer.writeEndElement();
			formatNewline();
		}
		if( visit.getLocationId() != null ){
			formatIndent();
			writer.writeStartElement("location");
			writer.writeCharacters(visit.getLocationId());
			writer.writeEndElement();
			formatNewline();
		}
		// TODO implement provider

		// TODO more data
		formatIndent();
		writer.writeStartElement(JAXBObservationSupplier.FACT_WRAPPER);
		formatNewline();
		formatPush();
		this.prevVisit = visit;
	}
	private void endEncounter() throws XMLStreamException{
		formatPop();
		formatIndent();
		writer.writeEndElement(); // fact wrapper
		formatNewline();
		formatPop();
		formatIndent();
		writer.writeEndElement(); // encounter
		formatNewline();
	}
	private void endPatient() throws XMLStreamException{
		formatPop();
		formatIndent();
		writer.writeEndElement();
		formatNewline();
	}
	private void startPatient(Patient patient) throws XMLStreamException{
		formatIndent();
		writer.writeStartElement(JAXBObservationSupplier.PATIENT_ELEMENT);
		writer.writeAttribute("id", patient.getId());
		formatNewline();
		formatPush();
		
		// TODO: how write dedicated source information
		// write surname
		// write given name
		
		// gender
		if( patient.getSex() != null ){
			formatIndent();
			writer.writeStartElement("gender");
			// TODO use acronym/special value
			writer.writeCharacters(patient.getSex().name());
			writer.writeEndElement();
			formatNewline();
		}
		
		// birth date
		if( patient.getBirthDate() != null ){
			formatIndent();
			writer.writeStartElement("birthdate");
			writer.writeCharacters(patient.getBirthDate().toPartialIso8601());
			writer.writeEndElement();
			formatNewline();
		}
		
		if( patient.getDeathDate() != null ){
			formatIndent();
			writer.writeStartElement("deathdate");
			writer.writeCharacters(patient.getDeathDate().toPartialIso8601());
			writer.writeEndElement();
			formatNewline();
		}
		prevPatient = patient;
		
		prevVisit = null; // clear previous encounter
	}
	@Override
	protected void acceptOrException(Observation observation) throws ObservationException {
		Patient thisPatient = observation.getExtension(Patient.class);
		Visit thisVisit = observation.getExtension(Visit.class);
		try {
			if( prevPatient == null ){
				// TODO write start document, meta, patient
				startPatient(thisPatient);
			}else if( !prevPatient.getId().equals(thisPatient.getId()) ){
				// close patient, write new patient
				endEncounter();
				endPatient();
				startPatient(thisPatient);
			}else{
				// same patient as previous fact
				// nothing to do
			}
			
			if( prevVisit == null ){
				// first visit for patient
				startEncounter(thisVisit);
			}else if( !prevVisit.getId().equals(thisVisit.getId()) ){
				endEncounter(); // close previous encounter
				startEncounter(thisVisit);
			}else{
				// same encounter as previous fact
				// nothing to do
			}
			// TODO clone observation, remove patient/encounter/source information as it is contained in wrappers
			formatIndent();
			marshaller.marshal(observation, writer);
			formatNewline();
			
		} catch (JAXBException | XMLStreamException e ) {
			throw new ObservationException(observation, e);
		}
		
	}
	
	@Override
	public void close() throws IOException{
		try {
			if( prevPatient != null ){
				// at least one fact processed
				// end facts, encounter, patient
				for( int i=0; i<3; i++){
					formatPop();
					formatIndent();
					writer.writeEndElement();
					formatNewline();
				}
			}
			writer.writeEndDocument(); // automatically closes root element
			writer.close();
			output.close();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

}
