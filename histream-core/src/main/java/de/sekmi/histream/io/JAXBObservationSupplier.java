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
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.impl.ObservationImpl;

public class JAXBObservationSupplier implements ObservationSupplier {
	private static final String DOCUMENT_ROOT = "eav-data";
	private static final String PATIENT_ELEMENT = "patient";
	private static final String ENCOUNTER_ELEMENT = "encounter";
	private static final String FACT_WRAPPER = "facts";
	
	

	private ObservationFactory factory;
	private Unmarshaller unmarshaller;
	private XMLStreamReader reader;
	private Map<String,String> meta;
	// patient data
	private String patientId;
	private Map<String,String> patientData;
	
	// encounter data
	private String encounterId;
	private DateTimeAccuracy encounterStart;
	private DateTimeAccuracy encounterEnd;
	private Map<String,String> visitData;
	
	
	
	
	public JAXBObservationSupplier(ObservationFactory factory, InputStream input)throws JAXBException, XMLStreamException, FactoryConfigurationError{
		this(factory, XMLInputFactory.newInstance().createXMLStreamReader(input));
	}
	public JAXBObservationSupplier(ObservationFactory factory, XMLStreamReader reader) throws JAXBException, XMLStreamException{
		this.factory = factory;
		this.meta = new HashMap<>();
		this.patientData = new HashMap<>();
		this.visitData = new HashMap<>();
		unmarshaller = JAXBContext.newInstance(ObservationImpl.class).createUnmarshaller();
		// TODO: set schema
		//unmarshaller.setSchema(schema);
		this.reader = reader;
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
	
	private void readMeta()throws XMLStreamException{
		if( !reader.isStartElement() || !reader.getLocalName().equals("meta") )return;
		
		// read meta
		reader.nextTag();
		if( reader.getLocalName().equals("etl") ){
			String etlStrategy = reader.getAttributeValue(null, "strategy");
			// TODO use constants for etl.strategy, etc.
			if( etlStrategy != null )meta.put("etl.strategy", etlStrategy);
			reader.nextTag();
			// should be end element
			reader.nextTag();
		}
		if( reader.getLocalName().equals("source") ){
			meta.put("source.timestamp", reader.getAttributeValue(null, "timestamp"));
			meta.put("source.id", reader.getAttributeValue(null, "id"));

			reader.nextTag();
			// should be end element
			reader.nextTag();
		}

		// skip to end of meta
		while( !reader.isEndElement() || !reader.getLocalName().equals("meta") ){
			reader.next();
		}
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
		}
		if( visitData.containsKey("end") ){
			encounterEnd = DateTimeAccuracy.parsePartialIso8601(visitData.get("end"));
		}
		// TODO assert at <facts>
		reader.nextTag();
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
			// TODO: set factory
		} catch (JAXBException e) {
			throw new XMLStreamException( e);
		}
		if( fact.getPatientId() != null && !fact.getPatientId().equals(patientId) ){
			throw new XMLStreamException("Fact patid differs from patient id", reader.getLocation());
		}
		
		fact.setPatientId(patientId);
		fact.setEncounterId(encounterId);
	
		if( fact.getStartTime() == null ){
			fact.setStartTime(encounterStart);
			fact.setEndTime(encounterEnd);
		}
		// TODO set etc. from visit
		
		// TODO set ObservationFactory, initialize extensions
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
	public String getMeta(String key) {
		return meta.get(key);
	}

	@Override
	public void close() throws XMLStreamException {
		reader.close();
	}
}
