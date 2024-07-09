package de.sekmi.histream.etl.config;

import java.util.Collections;
import java.util.List;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.FactRow;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.VisitPatientImpl;

/**
 * Row of patient data
 * TODO: implement {@link Patient}
 * @author Raphael
 *
 */
public class PatientRow implements FactRow{
	// TODO concepts
	private List<Observation> facts;
	private String patientId;
	String givenName;
	String surname;
	Sex sex;
	DateTimeAccuracy birthdate;
	DateTimeAccuracy deathdate;

	private PatientImpl patient;

	public PatientRow(String id){
		this.patientId = id;
	}
	
	@Override
	public List<Observation> getFacts() {
		return Collections.emptyList();
	}

	public PatientImpl getPatient() {
		return this.patient;
	}

	@Override
	public String getPatientId() {
		return patientId;
	}


	@Override
	public String getVisitId() {
		// no visit id for patient facts
		return null;
	}

	@Override
	public void createFacts(PatientImpl patient, VisitPatientImpl visit, ObservationFactory factory) {
		if( patient != null || visit != null ) {
			throw new IllegalArgumentException("patient and visit arguments must be null (as patient will be created)");
		}
		// patient and visit should be null
		this.patient = new PatientImpl(patientId, sex, birthdate, deathdate);
		this.patient.setSurname(surname);
		this.patient.setGivenName(givenName);
		// nothing to do for facts. There can be no facts without visit
	}
}