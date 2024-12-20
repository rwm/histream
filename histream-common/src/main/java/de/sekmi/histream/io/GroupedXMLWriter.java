package de.sekmi.histream.io;

import java.io.OutputStream;
import java.time.ZoneId;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.w3c.dom.DOMException;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.ExternalSourceImpl;
import de.sekmi.histream.impl.GroupedObservationHandler;
import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.impl.ObservationImpl;
import de.sekmi.histream.xml.DateTimeAccuracyAdapter;

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
	private ZoneId zoneId;
	/**
	 *  will be set during beginStream and used subsequently
	 */
	private ExternalSourceType metaSource;
	
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
		// this does not work with the DOM stream writer
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

	/**
	 * Set the timezone id for output of timestamp values.
	 * @param timeZone zone id or {@code null} to omit zone offset information
	 */
	private void setZoneId(ZoneId timeZone){
		this.zoneId = timeZone;
		// modify marshaller to use the timezone for timestamps
		DateTimeAccuracyAdapter a = new DateTimeAccuracyAdapter();
		a.setZoneId(zoneId);
		marshaller.setAdapter(DateTimeAccuracyAdapter.class, a);
	}
	
	@Override
	protected void beginStream() throws ObservationException{
		try {
			writer.writeStartDocument();
// this will write duplicate xmlns for the stream writer
//			writer.setPrefix(XMLConstants.DEFAULT_NS_PREFIX, NAMESPACE);
			writer.setDefaultNamespace(NAMESPACE);
			// NamespaceContext is not supported by DOM stream writer
			writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,GroupedXMLReader.DOCUMENT_ROOT,NAMESPACE);
			writer.setPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			writer.writeDefaultNamespace(NAMESPACE);
			writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			formatNewline();
			formatPush();
			// doesn't work for DOM writers
			//Objects.requireNonNull(writer.getPrefix(NAMESPACE));
			formatIndent();
			marshaller.marshal(meta, writer);
			this.metaSource = meta.getSource();
			formatNewline();
		} catch (XMLStreamException | JAXBException e) {
			throw new ObservationException(e);
		}
	}
	@Override
	public void setMeta(String key, String value, String scope) {
		meta.set(key, value, scope);

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
			// TODO write elements with namespaces -> ObservationImpl.XML_NAMESPACE, 
			writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,GroupedXMLReader.ENCOUNTER_ELEMENT,NAMESPACE);
			writer.setDefaultNamespace(NAMESPACE);
//			writer.setPrefix(XMLConstants.DEFAULT_NS_PREFIX, NAMESPACE);
//			writer.writeDefaultNamespace(NAMESPACE);
			writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
//			writer.writeStartElement(GroupedXMLReader.ENCOUNTER_ELEMENT);
			writer.writeAttribute("id", visit.getId());
			formatNewline();
			formatPush();
	
			if( visit.getStartTime() != null ){
				formatIndent();
				writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,"start",NAMESPACE);
				writer.writeCharacters(visit.getStartTime().toPartialIso8601(zoneId));
				writer.writeEndElement();
				formatNewline();
			}
			if( visit.getEndTime() != null ){
				formatIndent();
				writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,"end",NAMESPACE);
				writer.writeCharacters(visit.getEndTime().toPartialIso8601(zoneId));
				writer.writeEndElement();
				formatNewline();
			}
			if( visit.getLocationId() != null ){
				formatIndent();
				writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,"location",NAMESPACE);
				writer.writeCharacters(visit.getLocationId());
				writer.writeEndElement();
				formatNewline();
			}
			// TODO implement provider

			// write source timestamp
			ExternalSourceType es = visitSourceWithContext(visit, metaSource);
			if( es != null )try {
				formatIndent();
				marshaller.marshal(es, writer);
				formatNewline();
			} catch (JAXBException e) {
				throw new ObservationException(e);
			}
			// TODO more data
//			formatIndent();
//			writer.writeStartElement(GroupedXMLReader.FACT_WRAPPER);
//			formatNewline();
//			formatPush();
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
	}
	@Override
	protected void endEncounter(Visit visit) throws ObservationException{
		formatPop();
		try {
//			formatIndent();
//			writer.writeEndElement(); // fact wrapper
//			formatNewline();
//			formatPop();
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
			writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,GroupedXMLReader.PATIENT_ELEMENT,NAMESPACE);
			writer.writeAttribute("id", patient.getId());
			formatNewline();
			formatPush();
			
			// TODO: how write dedicated source information

			// write given name
			if( patient.getGivenName() != null ){
				formatIndent();
				writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,"given-name",NAMESPACE);
				writer.writeCharacters(patient.getGivenName());
				writer.writeEndElement();
				formatNewline();
			}

			// write surname
			if( patient.getSurname() != null ){
				formatIndent();
				writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,"surname",NAMESPACE);
				writer.writeCharacters(patient.getSurname());
				writer.writeEndElement();
				formatNewline();
			}


			// gender
			if( patient.getSex() != null ){
				formatIndent();
				writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,"gender",NAMESPACE);
				// TODO use acronym/special value
				writer.writeCharacters(patient.getSex().name());
				writer.writeEndElement();
				formatNewline();
			}
			
			// birth date
			if( patient.getBirthDate() != null ){
				formatIndent();
				writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,"birthdate",NAMESPACE);
				writer.writeCharacters(patient.getBirthDate().toPartialIso8601(zoneId));
				writer.writeEndElement();
				formatNewline();
			}
			
			// deceased status / death date
			if( patient.getDeceased() != null && patient.getDeceased() == true ){
				formatIndent();
				writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,"deceased",NAMESPACE);
				if( patient.getDeathDate() != null ){
					writer.writeCharacters(patient.getDeathDate().toPartialIso8601(zoneId));
				}
				writer.writeEndElement();
				formatNewline();
			}
			
			// write source timestamp
			ExternalSourceType es = patientSourceWithContext(patient,metaSource);
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
	
	/**
	 * Return a source object which represents the difference between the provided context and the patient source.
	 * If the patient source is equal to the context, null is returned.
	 * Otherwise, a source object is returned with the parts of the patient source which differ from the context.
	 * @param patient
	 * @param context
	 * @return
	 */
	private static ExternalSourceType patientSourceWithContext(Patient patient, ExternalSourceType context){
		// TODO also write source id if different from meta
		if( context == patient.getSource() || context.equals(patient.getSource()) ) {
			return null;
		}else {
			return patient.getSource();
		}
//		if( patient.getSourceTimestamp() != null 
//				&& (context == null
//					|| context.getSourceTimestamp() == null 
//					|| !patient.getSourceTimestamp().equals(context.getSourceTimestamp())) )
//		{
//			return new ExternalSourceImpl(null, patient.getSourceTimestamp());
//		}else{
//			return null;
//		}
	}
	private static ExternalSourceType visitSourceWithContext(Visit visit, ExternalSourceType context){
		// TODO also write source id if different from meta
		if( context == visit.getSource() || context.equals(visit.getSource()) ) {
			return null;
		}else {
			return visit.getSource();
		}
//		if( visit.getSourceTimestamp() != null 
//				&& (context == null
//					|| context.getSourceTimestamp() == null 
//					|| !visit.getSourceTimestamp().equals(context.getSourceTimestamp())) )
//		{
//			return new ExternalSourceImpl(null, visit.getSourceTimestamp());
//		}else{
//			return null;
//		}
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
		o.removeContext(visit.getStartTime(), source);
		marshaller.marshal(o, writer);
		// XXX fillContext?
		o.fillFromContext(visit.getStartTime(), source);
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
			marshalFactWithContext(observation, observation.getVisit(), metaSource);
			formatNewline();
		} catch (JAXBException | XMLStreamException | DOMException e) {
			ObservationException wrapped = new ObservationException(e);
			wrapped.setObservation(observation, "count/"+this.observationCount);
			throw wrapped;
		}
		this.observationCount ++;
	}
	public int getObservationCount(){
		return this.observationCount;
	}

}
