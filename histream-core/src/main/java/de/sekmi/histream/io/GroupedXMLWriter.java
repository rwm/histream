package de.sekmi.histream.io;

import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.ExternalSourceImpl;
import de.sekmi.histream.impl.GroupedObservationHandler;
import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.impl.ObservationImpl;

/**
 * Writes observations to a single XML file. Observations must be grouped by patient and encounter.
 * TODO fix duplicate namespace prefixes with JAXB marshaller
 * 
 * @author Raphael
 *
 */
public class GroupedXMLWriter extends GroupedObservationHandler{
	public static final String NAMESPACE = ObservationImpl.XML_NAMESPACE;//"http://sekmi.de/histream/ns/eav-data";
	private Marshaller marshaller;
	private boolean writeFormatted;
	private int formattingDepth;
	private Meta meta;
	private int observationCount;
	
	/**
	 * Used to output XML
	 */
	protected XMLStreamWriter writer;
	
	/**
	 * Constructor which doesn't initialize the {@link #writer}.
	 * Use this constructor to extend the class and provide a custom {@link XMLStreamWriter}.
	 * 
	 * @throws XMLStreamException initialisation error
	 */
	protected GroupedXMLWriter() throws XMLStreamException{
		try {
			this.marshaller = JAXBContext.newInstance(ObservationImpl.class, Meta.class).createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		} catch (JAXBException e) {
			throw new XMLStreamException(e);
		}
		this.writeFormatted = true;
		this.meta = new Meta();
		this.observationCount = 0;
	}
	/**
	 * Constructor to write XML to an {@link OutputStream}.
	 * Calling {@link #close()} will NOT close the specified output stream. 
	 * The stream must be closed separately.
	 * 
	 * @param output output stream
	 * @throws XMLStreamException initialisation error
	 */
	public GroupedXMLWriter(OutputStream output) throws XMLStreamException{
		this();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		// enable repairing namespaces to remove duplicate namespace declarations by JAXB marshal
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		this.writer = factory.createXMLStreamWriter(output);
	}

	/**
	 * Constructor to write XML to a {@link Result}
	 * @param result result to receive XML stream events
	 * @throws XMLStreamException initialisation error
	 */
	public GroupedXMLWriter(Result result)throws XMLStreamException{
		this();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		// enable repairing namespaces to remove duplicate namespace declarations by JAXB marshal
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		this.writer = factory.createXMLStreamWriter(result);
	}
	
	/**
	 * Configure whether to write whitespace and newline for human readable output
	 * @param formattedOutput true for human readable output
	 */
	public void setFormatted(boolean formattedOutput){
		this.writeFormatted = formattedOutput;
		try {
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		} catch (PropertyException e) {
		}
	}
	
	@Override
	protected void beginStream() throws ObservationException{
		try {
			//writer.setPrefix(XMLConstants.DEFAULT_NS_PREFIX, NAMESPACE);
			writer.writeStartDocument();
			writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,GroupedXMLReader.DOCUMENT_ROOT,NAMESPACE);
			writer.setDefaultNamespace(NAMESPACE);
			writer.writeDefaultNamespace(NAMESPACE);
			writer.setPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			formatNewline();
			formatPush();
			// doesn't work for DOM writers
			//Objects.requireNonNull(writer.getPrefix(NAMESPACE));
			formatIndent();
			marshaller.marshal(meta, writer);
			formatNewline();
		} catch (XMLStreamException | JAXBException e) {
			throw new ObservationException(e);
		}
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

	@Override
	protected void beginEncounter(Visit visit)throws ObservationException{
		try {
			formatIndent();
			writer.writeStartElement(GroupedXMLReader.ENCOUNTER_ELEMENT);
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

			// write source timestamp
			ExternalSourceImpl es = visitSourceWithContext(visit, meta.source);
			if( es != null )try {
				formatIndent();
				marshaller.marshal(es, writer);
				formatNewline();
			} catch (JAXBException e) {
				throw new ObservationException(e);
			}
			// TODO more data
			formatIndent();
			writer.writeStartElement(GroupedXMLReader.FACT_WRAPPER);
			formatNewline();
			formatPush();
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
	}
	@Override
	protected void endEncounter(Visit visit) throws ObservationException{
		formatPop();
		try {
			formatIndent();
			writer.writeEndElement(); // fact wrapper
			formatNewline();
			formatPop();
			formatIndent();
			writer.writeEndElement(); // encounter
			formatNewline();
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
	}
	@Override
	protected void endPatient(Patient patient) throws ObservationException{
		formatPop();
		try {
			formatIndent();
			writer.writeEndElement();
			formatNewline();
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
	}
	@Override
	protected void beginPatient(Patient patient) throws ObservationException{
		try{
			formatIndent();
			writer.writeStartElement(GroupedXMLReader.PATIENT_ELEMENT);
			writer.writeAttribute("id", patient.getId());
			formatNewline();
			formatPush();
			
			// TODO: how write dedicated source information

			// write given name
			if( patient.getGivenName() != null ){
				formatIndent();
				writer.writeStartElement("given-name");
				writer.writeCharacters(patient.getGivenName());
				writer.writeEndElement();
				formatNewline();
			}

			// write surname
			if( patient.getSurname() != null ){
				formatIndent();
				writer.writeStartElement("surname");
				writer.writeCharacters(patient.getSurname());
				writer.writeEndElement();
				formatNewline();
			}


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
			
			// deceased status / death date
			if( patient.getDeceased() != null && patient.getDeceased() == true ){
				formatIndent();
				writer.writeStartElement("deceased");
				if( patient.getDeathDate() != null ){
					writer.writeCharacters(patient.getDeathDate().toPartialIso8601());
				}
				writer.writeEndElement();
				formatNewline();
			}
			
			// write source timestamp
			ExternalSourceImpl es = patientSourceWithContext(patient,meta.source);
			if( es != null )try {
				formatIndent();
				marshaller.marshal(es, writer);
				formatNewline();
			} catch (JAXBException e) {
				throw new ObservationException(e);
			}
			
			
			// TODO use external source for patient
		}catch( XMLStreamException e ){
			throw new ObservationException(e);
		}
	}
	
	private static ExternalSourceImpl patientSourceWithContext(Patient patient, ExternalSourceType context){
		// TODO also write source id if different from meta
		if( patient.getSourceTimestamp() != null 
				&& (context == null
					|| context.getSourceTimestamp() == null 
					|| !patient.getSourceTimestamp().equals(context.getSourceTimestamp())) )
		{
			return new ExternalSourceImpl(null, patient.getSourceTimestamp());
		}else{
			return null;
		}
	}
	private static ExternalSourceImpl visitSourceWithContext(Visit visit, ExternalSourceType context){
		// TODO also write source id if different from meta
		if( visit.getSourceTimestamp() != null 
				&& (context == null
					|| context.getSourceTimestamp() == null 
					|| !visit.getSourceTimestamp().equals(context.getSourceTimestamp())) )
		{
			return new ExternalSourceImpl(null, visit.getSourceTimestamp());
		}else{
			return null;
		}
	}
	/**
	 * Marshal a fact without writing context information from patient, visit and source.
	 *
	 * @param fact fact
	 * @param visit visit context
	 * @param source source context
	 * @throws JAXBException errors during marshal operation
	 */
	private void marshalFactWithContext(Observation fact, Visit visit, ExternalSourceType source) throws JAXBException{
		// clone observation, remove patient/encounter/source information as it is contained in wrappers
		ObservationImpl o = (ObservationImpl)fact;
		o = o.clone();
		o.removeContext(o.getPatientId(), o.getEncounterId(), visit.getStartTime(), source);
		marshaller.marshal(o, writer);
	}

	@Override
	protected void endStream() throws ObservationException{
		try {
			writer.writeEndDocument(); // automatically closes root element
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}		
	}
	@Override
	public void close(){
		if( this.observationCount == 0 ){
			// no observations, file is empty
			// write meta data
			try {
				beginStream();
			} catch (ObservationException e) {
				reportError(e);
			}
			// endStream will be called by super.close()
		}
		super.close();
		try {
			if( writer != null ){
				writer.close();
				writer = null;
			}
		} catch (XMLStreamException e) {
			reportError(new ObservationException(e));
		}
	}
	@Override
	protected void onObservation(Observation observation) throws ObservationException {
		try {
			formatIndent();
			marshalFactWithContext(observation, observation.getExtension(Visit.class), meta.source);
			formatNewline();
		} catch (JAXBException | XMLStreamException e) {
			throw new ObservationException(e);
		}
		this.observationCount ++;
	}

}
