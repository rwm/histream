package de.sekmi.histream.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ExtensionAccessor;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.ObservationImpl;

/**
 * Read grouped observations from XML
 * 
 * @see GroupedXMLWriter
 * @author Raphael
 *
 */
public class GroupedXMLReader  implements ObservationSupplier {
	static final String DOCUMENT_ROOT = "eav-data";
	static final String PATIENT_ELEMENT = "patient";
	static final String ENCOUNTER_ELEMENT = "encounter";
	static final String FACT_WRAPPER = "facts";
	
	private ObservationFactory factory;
	private ExtensionAccessor<Patient> patientAccessor;
	private ExtensionAccessor<Visit> visitAccessor;
	private Patient currentPatient;
	private Visit currentVisit;
	private Meta meta;

	private Unmarshaller unmarshaller;
	private XMLStreamReader reader;
	// patient data
	private String patientId;
	private Map<String,String> patientData;

	// encounter data
	private String encounterId;
	private DateTimeAccuracy encounterStart;
	private DateTimeAccuracy encounterEnd;
	private Map<String,String> visitData;

	public GroupedXMLReader(ObservationFactory factory, InputStream input)throws JAXBException, XMLStreamException, FactoryConfigurationError{
		this(factory, XMLInputFactory.newInstance().createXMLStreamReader(input));
	}
	public GroupedXMLReader(ObservationFactory factory, XMLStreamReader reader) throws JAXBException, XMLStreamException{
		super();
		this.factory = factory;
		this.patientData = new HashMap<>();
		this.visitData = new HashMap<>();
		unmarshaller = JAXBContext.newInstance(ObservationImpl.class,Meta.class).createUnmarshaller();
		// TODO: set schema
		//unmarshaller.setSchema(schema);
		this.reader = reader;
		this.visitAccessor = factory.getExtensionAccessor(Visit.class);
		this.patientAccessor = factory.getExtensionAccessor(Patient.class);
		
		readToRoot();
		readMeta();
		readPatient();
		readEncounter();
	}
	
	private void readToRoot() throws XMLStreamException{
		while( reader.hasNext() ){
			reader.next();
			if( reader.isStartElement() )break;
		}
		if( !reader.isStartElement() || !reader.getLocalName().equals(DOCUMENT_ROOT) ){
			throw new XMLStreamException("Start element '"+DOCUMENT_ROOT+"' expected instead of "+reader.getLocalName(), reader.getLocation());
		}		
		reader.nextTag();
	}
	
	private void readMeta()throws XMLStreamException, JAXBException{
		if( !reader.isStartElement() || !reader.getLocalName().equals("meta") )return;
		this.meta = (Meta)unmarshaller.unmarshal(reader);
		
		reader.nextTag();
	}


	/**
	 * Reads patient element with content. Precondition patient start element. Postcondition encounter start element.
	 * @throws XMLStreamException on error
	 */
	private void readPatient() throws XMLStreamException{
		if( !reader.getLocalName().equals(PATIENT_ELEMENT) ){
			throw new XMLStreamException("Expected element: "+PATIENT_ELEMENT,reader.getLocation());
		}
		patientId = reader.getAttributeValue(null, "id");
		patientData.clear();
		reader.nextTag();
		while( reader.isStartElement() 
				&& !reader.getLocalName().equals(ENCOUNTER_ELEMENT) )
		{
			patientData.put(reader.getLocalName(), reader.getElementText());
			reader.nextTag();
		}
		// register with extension
		currentPatient = patientAccessor.accessStatic(patientId, (ExternalSourceType)meta.source);
		// TODO set patient data
		if( patientData.containsKey("birthdate") ){
			currentPatient.setBirthDate(DateTimeAccuracy.parsePartialIso8601(patientData.get("birthdate")));
		}
		if( patientData.containsKey("deathdate") ){
			currentPatient.setDeathDate(DateTimeAccuracy.parsePartialIso8601(patientData.get("deathdate")));
		}
		if( patientData.containsKey("gender") ){
			currentPatient.setSex(Sex.valueOf(patientData.get("gender")));
		}
		if( patientData.containsKey("surname") ){
			currentPatient.setSurname(patientData.get("surname"));
		}
		if( patientData.containsKey("given-name") ){
			currentPatient.setGivenName(patientData.get("given-name"));
		}
		
	}
	/**
	 * Reads encounter element with content. 
	 * Precondition encounter start element. 
	 * Postcondition facts start element.
	 * @throws XMLStreamException on error
	 */
	private void readEncounter() throws XMLStreamException{
		if( !reader.getLocalName().equals(ENCOUNTER_ELEMENT) ){
			throw new XMLStreamException("Expected element: "+ENCOUNTER_ELEMENT,reader.getLocation());
		}
		visitData.clear();
		encounterId = reader.getAttributeValue(null, "id");
		reader.nextTag();
		while( reader.isStartElement() 
				&& !reader.getLocalName().equals(FACT_WRAPPER) )
		{
			visitData.put(reader.getLocalName(), reader.getElementText());
			reader.nextTag();
		}
		if( visitData.containsKey("start") ){
			encounterStart = DateTimeAccuracy.parsePartialIso8601(visitData.get("start"));
		}else{
			encounterStart = null;
		}
		if( visitData.containsKey("end") ){
			encounterEnd = DateTimeAccuracy.parsePartialIso8601(visitData.get("end"));
		}else{
			encounterEnd = null;
		}
		// TODO assert at <facts>
		reader.nextTag();
		
		currentVisit = visitAccessor.accessStatic(encounterId, currentPatient, (ExternalSourceType)meta.source);
		currentVisit.setStartTime(encounterStart);
		currentVisit.setEndTime(encounterEnd);
		currentVisit.setLocationId(visitData.get("location"));
		// TODO set other visit data: gender, provider, in/out status
		visitData.get("provider");
		
	}
	protected Observation readObservation()throws XMLStreamException{
		if( reader.isWhiteSpace() ){
			reader.nextTag();
		}
		// </facts> might occur after previous call to readObservation()
		while( reader.isEndElement() ){
			switch( reader.getLocalName() ){
			case FACT_WRAPPER:
				// end of facts
				reader.nextTag();
				// fall through to end of visit, 
				// XXX this doesn't work, if facts are allowed outside of encounter (e.g. directly under patient)
			case ENCOUNTER_ELEMENT:
				// end of visit
				reader.nextTag();
				if( reader.isStartElement() && reader.getLocalName().equals(ENCOUNTER_ELEMENT) ){
					// next visit
					readEncounter();
				}
				break;
			case PATIENT_ELEMENT:
				// end of patient
				reader.nextTag();
				if( reader.isStartElement() && reader.getLocalName().equals(PATIENT_ELEMENT) ){
					readPatient();
					readEncounter();
				}
				break;
			case DOCUMENT_ROOT:
				// end of document
				return null;
			}
		}
		// start element of eav-item or eav-group
		if( !reader.isStartElement() ){
			throw new XMLStreamException("Element patient, encounter or fact expected instead of "+reader.getLocalName(), reader.getLocation());
		}
		if( reader.getLocalName().equals(PATIENT_ELEMENT) ){
			
		}
		ObservationImpl fact;
		try {
			fact = (ObservationImpl)unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			throw new XMLStreamException( e);
		}
		if( fact.getPatientId() != null && !fact.getPatientId().equals(patientId) ){
			throw new XMLStreamException("Fact patid differs from patient id", reader.getLocation());
		}
		
		fact.fillFromContext(patientId, encounterId, encounterStart, meta.source);
		
		// TODO set etc. from visit
		
		// set ObservationFactory, initialize extensions
		fact.setFactory((ObservationFactoryImpl)factory);
		patientAccessor.set(fact, currentPatient);
		visitAccessor.set(fact, currentVisit);
		// TODO add tests to verify patient/visit extensions for parsed facts
		
		return fact;
	}

	@Override
	public Observation get() {
		try {
			return readObservation();
		} catch (XMLStreamException e) {
			throw new UncheckedIOException(new IOException(e));
		}
	}
	
	@Override
	public void close() throws XMLStreamException {
		reader.close();
	}
	@Override
	public String getMeta(String key) {
		return meta.get(key);
	}
}
