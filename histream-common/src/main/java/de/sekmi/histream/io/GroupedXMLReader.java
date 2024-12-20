package de.sekmi.histream.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.impl.ExternalSourceImpl;
import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.impl.ObservationImpl;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.ScopedProperty;
import de.sekmi.histream.impl.VisitPatientImpl;
import de.sekmi.histream.xml.DateTimeAccuracyAdapter;

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
	static final String FACT_ELEMENT = "fact";
	public static final QName ROOT_ELEMENT = new QName(GroupedXMLWriter.NAMESPACE, DOCUMENT_ROOT);
	
//	private ObservationFactory factory;
	private PatientImpl currentPatient;
	private VisitPatientImpl currentVisit;
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
	private ZoneId zoneId;
	// will be filled during readMeta an used subsequently
	private ExternalSourceType metaSource;

	/**
	 * The provided {@code input} is not closed by a call to {@link #close()}
	 * 
	 * @param input XML input
	 * @param localZone zone to use for local timestamps. If a timezone is specified in the input metadata, the constructor argument is ignored and the input timezone used instead.
	 * @throws JAXBException JAXB error
	 * @throws XMLStreamException XML stream error
	 * @throws FactoryConfigurationError other error
	 */
	public GroupedXMLReader(InputStream input, ZoneId localZone)throws JAXBException, XMLStreamException, FactoryConfigurationError{
		this(XMLInputFactory.newInstance().createXMLStreamReader(input), localZone);
	}

	/**
	 * Construct a reader with a {@link XMLStreamReader}. The {@code reader} is closed when {@link #close()} is called.
	 * @param reader xml reader
	 * @param localZone zone to use for local timestamps. If a timezone is specified in the input metadata, the constructor argument is ignored and the input timezone used instead.
	 * @throws JAXBException jaxb error
	 * @throws XMLStreamException stream error
	 */
	public GroupedXMLReader(XMLStreamReader reader, ZoneId localZone) throws JAXBException, XMLStreamException{
		super();
//		this.factory = factory;
		this.patientData = new HashMap<>();
		this.visitData = new HashMap<>();
		unmarshaller = JAXBContext.newInstance(ObservationImpl.class,Meta.class).createUnmarshaller();
		
		// TODO: set schema
		//unmarshaller.setSchema(schema);
		this.reader = reader;
		readToRoot();
		readMeta();
		Objects.requireNonNull(metaSource,"meta properties for source missing");
		Objects.requireNonNull(metaSource.getSourceId(),"meta source id missing");

		// prefer timezone defined in metadata
		if( this.zoneId == null ) {
			// no timezone in metadata, use provided local zone
			this.zoneId = localZone;
		}
		if( zoneId != null ){
			// modify marshaller to use the timezone for timestamps
			DateTimeAccuracyAdapter a = new DateTimeAccuracyAdapter();
			a.setZoneId(zoneId);
			unmarshaller.setAdapter(DateTimeAccuracyAdapter.class, a);			
		}
	
		if( reader.isEndElement() ){
			// no patient/encounter/facts
			return;
		}
		readPatient();
		readEncounter();
	}

	public ZoneId getMetaTimezone() {
		return metaSource.getSourceZone();
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
		this.metaSource = this.meta.getSource();
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
				&& !reader.getLocalName().equals(ENCOUNTER_ELEMENT)
				&& !reader.getLocalName().equals("source") )
		{
			patientData.put(reader.getLocalName(), reader.getElementText());
			reader.nextTag();
		}
		
		// read source information
		ExternalSourceType es = readSource();
		
		currentPatient = newPatient(patientId, (ExternalSourceType)es);
		
		// set patient data
		if( patientData.containsKey("birthdate") ){
			String dob = patientData.get("birthdate");
			try {
				currentPatient.setBirthDate(DateTimeAccuracy.parsePartialIso8601(dob, zoneId));
			} catch (ParseException e) {
				throw new XMLStreamException("Unable to parse birthdate: "+dob, reader.getLocation(), e);
			}
		}
		if( patientData.containsKey("deceased") ){
			// patient known to be deceased
			currentPatient.setDeceased(true);
			String date = patientData.get("deceased");
			// will be empty string for <deceased/>
			if( date != null && date.length() != 0 ){
				try {
					currentPatient.setDeathDate(DateTimeAccuracy.parsePartialIso8601(date, zoneId));
				} catch (ParseException e) {
					throw new XMLStreamException("Unable to parse deceased date: "+date, reader.getLocation(), e);
				}
			}
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
		// TODO add more data. e.g. zip code
	}
	
	/**
	 * Read source element and amend with meta.source. Needed for patient and encounter.
	 * @return source. Missing data is added from meta.
	 * @throws XMLStreamException on errro
	 */
	private ExternalSourceType readSource() throws XMLStreamException{
		ExternalSourceType es;
		if( reader.getLocalName().equals("source") ){
			try {
				es = (ExternalSourceImpl) unmarshaller.unmarshal(reader);
				if( reader.getEventType() != XMLStreamConstants.START_ELEMENT ){
					reader.nextTag();
				}
			} catch (JAXBException e) {
				throw new XMLStreamException("Unable to parse patient source", reader.getLocation(), e);
			}
			// add missing fields from meta
			if( es.getSourceId() == null ){
				es.setSourceId(metaSource.getSourceId());
			}
			if( es.getSourceTimestamp() == null ){
				es.setSourceTimestamp(metaSource.getSourceTimestamp());
			}
		}else{
			// use meta source
			es = metaSource;
		}
		return es;
	}

	private PatientImpl newPatient(String id, ExternalSourceType source) {
		PatientImpl p = new PatientImpl(id);
		p.setSource(source);
		return p;
	}

	private VisitPatientImpl newVisit(String encounterId, PatientImpl patient, ExternalSourceType source) {
		VisitPatientImpl v = new VisitPatientImpl(encounterId, patient, null);
		v.setSource(source);
		return v;
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
				&& !reader.getLocalName().equals(FACT_ELEMENT)
				&& !reader.getLocalName().equals("source") )
		{
			visitData.put(reader.getLocalName(), reader.getElementText());
			reader.nextTag();
		}
		if( visitData.containsKey("start") ){
			String date = visitData.get("start");
			try {
				encounterStart = DateTimeAccuracy.parsePartialIso8601(date, zoneId);
			} catch (ParseException e) {
				throw new XMLStreamException("Unable to parse encounter/start: "+date, reader.getLocation(), e);
			}
		}else{
			encounterStart = null;
		}
		if( visitData.containsKey("end") ){
			String date = visitData.get("end");
			try {
				encounterEnd = DateTimeAccuracy.parsePartialIso8601(date, zoneId);
			} catch (ParseException e) {
				throw new XMLStreamException("Unable to parse encounter/end: "+date, reader.getLocation(), e);
			}
		}else{
			encounterEnd = null;
		}
		ExternalSourceType es = readSource();
		
		// TODO assert at <facts>
		//reader.nextTag();

		currentVisit = newVisit(encounterId, currentPatient, (ExternalSourceType)es);

		currentVisit.setStartTime(encounterStart);
		currentVisit.setEndTime(encounterEnd);
		currentVisit.setLocationId(visitData.get("location"));
		

		// TODO set other visit data: provider, in/out status
		currentVisit.setProviderId(visitData.get("provider"));
		
	}
	protected Observation readObservation()throws XMLStreamException{
		if( reader.isWhiteSpace() ){
			reader.nextTag();
		}
		// </facts> might occur after previous call to readObservation()
		while( reader.isEndElement() ){
			switch( reader.getLocalName() ){
			/*case FACT_WRAPPER:
				// end of facts
				reader.nextTag();
				*/
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
		
		fact.fillFromContext(encounterStart, metaSource);
		
		// set ObservationFactory, initialize extensions
		fact.setVisit(currentVisit);
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
	
	/**
	 * Closes the {@link XMLStreamReader} which was used.
	 * This does not close any {@link InputStream} provided in the constructor.
	 */
	@Override
	public void close() throws XMLStreamException {
		reader.close();
	}
	@Override
	public String getMeta(String key, String path) {
		return meta.getValue(key, path);
	}

	@Override
	public Iterable<ScopedProperty> getMeta() {
		return meta.properties;
	}
}
