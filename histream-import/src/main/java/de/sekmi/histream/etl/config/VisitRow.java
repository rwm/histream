package de.sekmi.histream.etl.config;

import java.util.List;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.VisitPatientImpl;

public class VisitRow extends AbstractFactRow{
	DateTimeAccuracy start;
	DateTimeAccuracy end;
	String location;
	String recordOrigin;
	String provider;
	private VisitPatientImpl impl;

	public VisitRow(String visitId, String patientId, DateTimeAccuracy startTime){
		super(patientId,visitId);
		this.start = startTime;
	}
	@Override
	public List<Observation> getFacts() {
		return facts;
	}

	public VisitPatientImpl getVisit() {
		return impl;
	}
	@Override
	public void createFacts(PatientImpl patient, VisitPatientImpl visit, ObservationFactory factory) {
		if( visit != null ) {
			throw new IllegalArgumentException("visit argument must be null (as it will be created)");
		}
		impl = new VisitPatientImpl(getVisitId(), patient, start);
		impl.setEndTime(end);
		impl.setLocationId(location);
		impl.setProviderId(provider);
		// create facts
		super.createFacts(patient, impl, factory);
	}
}
