package de.sekmi.histream.etl.config;

import java.util.Arrays;
import java.util.List;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.VisitPatientImpl;

/**
 * Row from EAV table. Per definition, only
 * a single fact per row is contained.
 * 
 * @author R.W.Majeed
 *
 */
public class EavRow extends AbstractFactRow {
	/** the generated fact created by {@link #createFacts(PatientImpl, VisitPatientImpl)} */
	private Observation fact;

	String conceptId;
	DateTimeAccuracy start;
	DateTimeAccuracy end;
	Value value;

	String location;
	String provider;
	
	public EavRow(String patientId, String visitId, String conceptId){
		super(patientId,visitId);
		this.conceptId = conceptId;
	}
	@Override
	public List<Observation> getFacts() {
		return Arrays.asList(fact);
	}
	public Observation getFact(){
		return fact;
	}

	@Override
	public void createFacts(PatientImpl patient, VisitPatientImpl visit, ObservationFactory factory) {
		this.fact = factory.createObservation(visit, conceptId, start);
		fact.setEndTime(end);
		fact.setProviderId(provider);
		fact.setLocationId(location);
		fact.setValue(value);
		// TODO add modifiers

		
		fact.setSource(getSource());
		
	}

}
