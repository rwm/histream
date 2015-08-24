package de.sekmi.histream.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.AbstractObservationHandler;
import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.impl.ObservationImpl;

/**
 * Writes observations to a single XML file. Observations must be grouped by patient and encounter.
 * TODO write test case which reads+writes XML file and compares input+output literally.
 * TODO prevent duplicate namespace declarations by JAXB.marshal of facts
 * 
 * @author Raphael
 *
 */
public class XMLWriter extends AbstractObservationHandler implements Closeable {
	private static final String NAMESPACE = "http://sekmi.de/histream/ns/eav-data";
	private Patient prevPatient;
	private Visit prevVisit;
	private Marshaller marshaller;
	private XMLStreamWriter writer;
	private boolean writeFormatted;
	private int formattingDepth;
	private Meta meta;
	
	private XMLWriter() throws JAXBException{
		this.marshaller = JAXBContext.newInstance(ObservationImpl.class, Meta.class).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		this.writeFormatted = true;
		this.meta = new Meta();
	}
	public XMLWriter(OutputStream output) throws JAXBException, XMLStreamException, FactoryConfigurationError{
		this();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		// enable repairing namespaces to remove duplicate namespace declarations by JAXB marshal
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		this.writer = factory.createXMLStreamWriter(output);
	}

	private void writeStartDocument() throws XMLStreamException, JAXBException{
		writer.setDefaultNamespace(NAMESPACE);
		writer.setPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		writer.writeStartDocument();
		formatNewline();

		writer.writeStartElement(NAMESPACE, JAXBObservationSupplier.DOCUMENT_ROOT);
		writer.writeDefaultNamespace(NAMESPACE);
		writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		formatNewline();
		formatPush();
		
		formatIndent();
		marshaller.marshal(meta, writer);
		formatNewline();
	}
	@Override
	public void setMeta(String key, String value) {
		meta.set(key, value);
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
				// write start document, meta, patient
				writeStartDocument();
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
			formatIndent();
			marshalFactWithContext(observation, thisPatient, thisVisit, meta.source);
			formatNewline();
			
		} catch (JAXBException | XMLStreamException e ) {
			throw new ObservationException(observation, e);
		}
		
	}

	/**
	 * Marshal a fact without writing context information from patient, visit and source.
	 * TODO move method to separate helper class
	 *
	 * @param fact fact
	 * @param patient patient context
	 * @param visit visit context
	 * @param source source context
	 * @throws ObservationException for errors in fact
	 * @throws JAXBException errors during marshal operation
	 */
	public void marshalFactWithContext(Observation fact, Patient patient, Visit visit, ExternalSourceType source) throws ObservationException, JAXBException{
		// clone observation, remove patient/encounter/source information as it is contained in wrappers
		ObservationImpl o = (ObservationImpl)fact;
		o = o.clone();
		o.removeContext(patient.getId(), visit.getId(), source);
		marshaller.marshal(o, writer);
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
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

}
