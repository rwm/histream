package de.sekmi.histream.ontology.skos;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.io.Transformation;

public class RDFTransformation implements Transformation {

	private ObservationFactory factory;
	
	public RDFTransformation(ObservationFactory factory){
		this.factory = factory;
	}
	
	@Override
	public Optional<Observation> transform(Observation fact) {
		if( wantRemoved(fact) ) return Optional.empty();
		Observation n = factory.createObservation(fact.getPatientId(), fact.getConceptId(), fact.getStartTime());
		modify(n);
		return Optional.of(n);
	}

	@Override
	public Function<Observation, Observation> modificationFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate<Observation> allowedPredicate() {
		return new Predicate<Observation>() {
			@Override
			public boolean test(Observation t) {
				return !wantRemoved(t);
			}
		};
	}


	@Override
	public boolean wantRemoved(Observation fact) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean modify(Observation fact) {
		// TODO Auto-generated method stub
		return false;
	}

}
