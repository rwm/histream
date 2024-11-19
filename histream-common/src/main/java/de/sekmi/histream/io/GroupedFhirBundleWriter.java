package de.sekmi.histream.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Objects;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.w3c.dom.DOMException;

import de.sekmi.histream.Modifier;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.Value;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.ext.Visit;
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
public class GroupedFhirBundleWriter extends GroupedObservationHandler{
	private Meta meta;
	private int observationCount;
	private int patientCount;
	private int encounterCount;
	
	private ZoneId zoneId;
	/**
	 *  will be set during beginStream and used subsequently
	 */
	private ExternalSourceType metaSource;
	
	/**
	 * Used to output XML
	 */
	protected FhirBundleWriter w;
	
	/**
	 * Constructor which doesn't initialize the writer {@link #w}.
	 * Use this constructor to extend the class and provide a custom {@link XMLStreamWriter}.
	 * 
	 * @throws XMLStreamException initialisation error
	 */
	protected GroupedFhirBundleWriter() throws XMLStreamException{
		this.meta = new Meta();
		this.observationCount = 0;
	}
	/**
	 * Constructor to write XML to an {@link OutputStream}.
	 * Calling {@link #close()} will NOT close the specified output stream. 
	 * The stream must be closed separately.
	 * 
	 * @param output output stream
	 * @param charsetEncoding encoding for the output
	 * @throws XMLStreamException initialisation error
	 */
	public GroupedFhirBundleWriter(OutputStream output, String charsetEncoding) throws XMLStreamException{
		this();
		w = new FhirBundleWriter(output, charsetEncoding);
	}

	/**
	 * Constructor to write XML to a {@link Result}
	 * @param result result to receive XML stream events
	 * @throws XMLStreamException initialisation error
	 */
	public GroupedFhirBundleWriter(Result result)throws XMLStreamException{
		this();
		w = new FhirBundleWriter(result);
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
	}
	
	@Override
	protected void beginStream() throws ObservationException{
		this.metaSource = meta.getSource();
		Objects.requireNonNull(this.metaSource,"Metadata zone id required");
		this.zoneId = this.metaSource.getSourceZone();
		Objects.requireNonNull(this.zoneId, "Metadata zoneId undefined");
		try {
			w.writeStartDocument(null, null, null);
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
	}
	@Override
	public void setMeta(String key, String value, String scope) {
		meta.set(key, value, scope);
		
	}

	@Override
	protected void beginEncounter(Visit visit)throws ObservationException{
		try {
			w.beginBundleEntry("Encounter/"+getCurrentLocalEncounterId());
			w.writeStartElement("Encounter");
			// use integer sequence for local id
			w.writeStringValue("id", getCurrentLocalEncounterId());
			
			// write location to observation meta.source 
			if( visit.getLocationId() != null ) {
				w.writeStartElement("meta");
				try {
					w.writeStringValue("source", "urn:location:"+URLEncoder.encode(visit.getLocationId(),StandardCharsets.UTF_8.name()));
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(e);
				}
				// TODO we could also write lastUpdated timestamp from source.getSourceTimestamp
				w.writeEndElement();			
			}

			// write external id in identifier.value
			w.writeStartElement("identifier");
			w.writeStringValue("value", visit.getId());
			w.writeEndElement();
			// whether encounter is in-progress, finished, etc.
			w.writeStringValue("status", "completed");
			// whether inpatient, outpatient, emergency, etc.
			w.writeStartElement("class");
			w.writeStringValue("system", "http://terminology.hl7.org/CodeSystem/v3-ActCode");
			w.writeStringValue("code", "AMB");
			w.writeEndElement();
			
			// reference to patient
			w.writeStartElement("subject");
			w.writeStringValue("reference", "Patient/"+getCurrentLocalPatientId());
			w.writeEndElement();
/* 
				<status value="in-progress" />
				<class>
					<system	value="" />
					<code value="AMB" />
				</class>
				<subject>
					<reference value="Patient/p1" />
				</subject>

 */
			w.writeStartElement("period");
			if( visit.getStartTime() != null ){
				w.writeTimestamp("start", visit.getStartTime(), zoneId);
			}
			if( visit.getEndTime() != null ){
				w.writeTimestamp("end", visit.getEndTime(), zoneId);
			}
			w.writeEndElement();

			if( visit.getLocationId() != null ){
				w.writeStartElement("location");
				w.writeStartElement("location");
				w.writeStringValue("reference", "Location/"+visit.getLocationId());
				w.writeEndElement();
				w.writeEndElement();
			}
			// TODO implement provider

			// write source timestamp
			ExternalSourceType es = visitSourceWithContext(visit, metaSource);
			// TODO write source es
			w.writeEndElement();
			w.endBundleEntry();

		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
	}

	private String getCurrentLocalPatientId() {
		return Integer.toString(patientCount+1);
	}

	private String getCurrentLocalEncounterId() {
		return Integer.toString(encounterCount+1);
	}

	@Override
	protected void endEncounter(Visit visit) throws ObservationException{
		try {
			w.writeCommentLine("END Encounter/"+getCurrentLocalEncounterId()+": "+visit.getId());
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
		encounterCount ++;
	}
	@Override
	protected void endPatient(Patient patient) throws ObservationException{
		
		try {
			w.writeCommentLine("END Patient/"+getCurrentLocalPatientId()+": "+patient.getId());
			w.formatNewline();
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
		patientCount ++;
	}
	@Override
	protected void beginPatient(Patient patient) throws ObservationException{
		
		try{
			w.beginBundleEntry("Patient/"+getCurrentLocalPatientId());
			w.writeStartElement("Patient");
			
			// TODO: how write dedicated source information

			// local patient id is numeric sequence
			w.writeStringValue("id", getCurrentLocalPatientId());

			// real given id in identifier.value
			w.writeStartElement("identifier");
			w.writeStringValue("value", patient.getId());
			w.writeEndElement();

			// write given name
			if( patient.getGivenName() != null || patient.getSurname() != null ){
				w.writeStartElement("name");
				
				// write surname
				if( patient.getSurname() != null ){
					w.writeStringValue("family", patient.getSurname());
				}
				if( patient.getGivenName() != null ) {
					w.writeStringValue("given", patient.getGivenName());
				}
				w.writeEndElement();
			}



			// gender
			if( patient.getSex() != null ){
				String value = patient.getSex().name();
				if( patient.getSex() == Sex.indeterminate ) {
					value = "other"; 
				}
				w.writeStringValue("gender", value);
			}
			
			// birth date
			if( patient.getBirthDate() != null ){
				w.writeTimestamp("birthDate", patient.getBirthDate(), zoneId);
			}
			
			// deceased status / death date
			if( patient.getDeceased() != null && patient.getDeceased() == true ){
				if( patient.getDeathDate() != null ){
					w.writeTimestamp("deceasedDateTime", patient.getDeathDate(), zoneId);
				}else {
					w.writeStringValue("deceasedBoolean", Boolean.TRUE.toString());
				}
			}
			
//			// write source timestamp
//			ExternalSourceType es = patientSourceWithContext(patient,metaSource);
//			if( es != null ) {
//				formatIndent();
//				// TODO write timestamp
////				marshaller.marshal(es, writer);
//				formatNewline();
//			}
			
			w.writeEndElement();
			w.endBundleEntry();
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
	 * @throws XMLStreamException 
	 */
	private void marshalFactWithContext(Observation fact, Visit visit, ExternalSourceType source) throws  XMLStreamException{
		// clone observation, remove patient/encounter/source information as it is contained in wrappers
		ObservationImpl o = (ObservationImpl)fact;
//		o = o.clone();
//		o.removeContext(visit.getStartTime(), source);
		int seqId = this.observationCount+1;
		
		w.beginBundleEntry("Observation/"+Integer.toString(seqId));
		w.writeStartElement("Observation");
		w.writeStringValue("id", Integer.toString(seqId));
		
		// write location to observation meta.source 
		// write location to observation meta.source 
		if( o.getLocationId() != null ) {
			w.writeStartElement("meta");
			try {
				w.writeStringValue("source", "urn:location:"+URLEncoder.encode(o.getLocationId(),StandardCharsets.UTF_8.name()));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
			// TODO we could also write lastUpdated timestamp from source.getSourceTimestamp
			w.writeEndElement();			
		}

		w.writeStringValue("status", "final");
	
		// concept code
		w.writeStartElement("code");
		w.writeStartElement("coding");
		w.writeStringValue("code", o.getConceptId());
		w.writeEndElement();
		w.writeEndElement();
		
		// patient reference
		w.writeStartElement("subject");
		w.writeStringValue("reference", "Patient/"+getCurrentLocalPatientId());
		w.writeEndElement();
	
		// patient reference
		w.writeStartElement("encounter");
		w.writeStringValue("reference", "Encounter/"+getCurrentLocalEncounterId());
		w.writeEndElement();
	
		Objects.requireNonNull(o.getStartTime());
		if( o.getEndTime() != null ) {
			// use period
			w.writeStartElement("effectivePeriod");
			w.writeTimestamp("start", o.getStartTime(), zoneId);
			w.writeTimestamp("end", o.getEndTime(), zoneId);
			w.writeEndElement();
		}else {
			// only have start time
			w.writeTimestamp("effectiveDateTime", o.getStartTime(), zoneId);
		}

		Value v = o.getValue();
		if( v != null ) {
			w.writeValueTyped(v);
		}
		

		if( o.hasModifiers() ) {
			Iterator<Modifier> modifiers = o.getModifiers();
			while( modifiers.hasNext() ) {
				Modifier m = modifiers.next();
				
				w.writeStartElement("component");
				// write code
				w.writeStartElement("code");
				w.writeStartElement("coding");
				w.writeStringValue("code", m.getConceptId());
				w.writeEndElement();
				w.writeEndElement();

				if( m.getValue() != null ) {
					w.writeValueTyped(m.getValue());
				}
				w.writeEndElement();
			}
		}

		// TODO write observation
		w.writeEndElement();
		w.endBundleEntry();
		// XXX fillContext?
//		o.fillFromContext(visit.getStartTime(), source);
	}


	@Override
	protected void endStream() throws ObservationException{
		try {
			w.writer.writeEndDocument(); // automatically closes root element
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
			w.close();
		} catch (IOException e) {
			reportError(new ObservationException(e));
		}
	}
	@Override
	protected void onObservation(Observation observation) throws ObservationException {
		try {

			marshalFactWithContext(observation, observation.getVisit(), metaSource);
		} catch ( XMLStreamException | DOMException e) {
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
