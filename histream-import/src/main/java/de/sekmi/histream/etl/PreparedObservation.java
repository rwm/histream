package de.sekmi.histream.etl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.impl.VisitPatientImpl;

public class PreparedObservation {
	// patientId, visitId is known in factrow
	private DateTimeAccuracy start;
	private DateTimeAccuracy end;

	private String concept;
	private Value value;
	private String location;
	private String provider;
	
	private Map<String, Value> modifiers;

	// TODO fill from concept.create

	public void setStart(DateTimeAccuracy start) {
		this.start = start;
	}
	public void setEnd(DateTimeAccuracy end) {
		this.end = end;
	}
	public void setConcept(String concept) {
		this.concept = concept;
	}
	public void setValue(Value value) {
		this.value = value;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void addModifier(String modifier, Value value) {
		if( modifiers == null ) {
			modifiers = new HashMap<>();
		}
		if( modifiers.containsKey(modifier) ) {
			// duplicate modifier
			// TODO report warning
		}
		modifiers.put(modifier, value);
	}

	public Observation createObservation(VisitPatientImpl visit, ObservationFactory factory) {
		Objects.requireNonNull(factory);
		Objects.requireNonNull(visit);

		DateTimeAccuracy start = this.start;
		if( start == null ) {
			start = visit.getStartTime();
		}
		Observation o = factory.createObservation(visit, concept, start);
		o.setEndTime(end);
		o.setValue(value);
		o.setLocationId(location);
		if( this.modifiers != null ) {
			modifiers.forEach( o::addModifier );
		}
		return o;
	}
	
}
