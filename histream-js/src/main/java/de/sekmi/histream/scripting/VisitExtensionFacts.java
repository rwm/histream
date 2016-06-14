package de.sekmi.histream.scripting;

import java.util.Objects;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

public class VisitExtensionFacts extends AbstractFacts {
	private Patient patient;
	private Visit visit;
	
	public VisitExtensionFacts(ObservationFactory factory, Patient patient, Visit visit) {
		super(factory);
		Objects.requireNonNull(patient);
		Objects.requireNonNull(visit);
		this.patient = patient;
		this.visit = visit;
	}
	
	@Override
	protected Observation create(String conceptId) {
		Observation o = factory.createObservation(patient.getId(), conceptId, visit.getStartTime());
		o.setExtension(Patient.class, patient);
		o.setEncounterId(visit.getId());
		o.setExtension(Visit.class, visit);
		return o;
	}

}
