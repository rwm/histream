package de.sekmi.histream.etl.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.FactRow;
import de.sekmi.histream.etl.PreparedObservation;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.VisitPatientImpl;

public abstract class AbstractFactRow implements FactRow {
	protected List<Observation> facts;
	protected List<PreparedObservation> preparedObservations;

	private String patientId;
	private String visitId;
	ExternalSourceType source;
	String recordOrigin;

	public AbstractFactRow(String patientId, String visitId) {
		this.patientId = patientId;
		this.visitId = visitId;
	}
	
	@Override
	public List<Observation> getFacts(){
		return facts;
	}

	@Override
	public String getPatientId() {
		return patientId;
	}

	@Override
	public String getVisitId() {
		return visitId;
	}


	public void addPreparedObservation(PreparedObservation obs) {
		if( preparedObservations == null ) {
			preparedObservations = new ArrayList<>();
		}
		preparedObservations.add(obs);
	}

	@Override
	public void createFacts(PatientImpl patient, VisitPatientImpl visit, ObservationFactory factory) {
		Objects.requireNonNull(visit);
		Objects.requireNonNull(factory);
		// default implementation creates observations from preparedObservations
		if( preparedObservations == null ) {
			facts = Collections.emptyList();
			return; // nothing to do
		}
		facts = new ArrayList<>(preparedObservations.size());
		for( PreparedObservation po : preparedObservations ) {
			// throw error if visit id differs from visit.id
			if( visit.getId().equals(visitId) == false ) {
				throw new IllegalStateException("Visit id does not match fact's visit id");
			}
			Observation o = po.createObservation(visit, factory);
			o.setSource(getSource());
			facts.add(o);
		}
	}
	public ExternalSourceType getSource() {
		return this.source;
	}
}
