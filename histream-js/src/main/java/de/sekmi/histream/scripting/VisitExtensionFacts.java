package de.sekmi.histream.scripting;

import java.util.Objects;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.VisitPatientImpl;

public class VisitExtensionFacts extends AbstractFacts {
	private VisitPatientImpl visit;
	
	public VisitExtensionFacts(ObservationFactory factory, VisitPatientImpl visit) {
		super(factory);
		Objects.requireNonNull(visit);
		this.visit = visit;
	}
	
	@Override
	protected Observation create(String conceptId) {
		Observation o = factory.createObservation(visit, conceptId, visit.getStartTime());
		return o;
	}

}
