package de.sekmi.histream.ontology.skos;

import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.io.Transformation;

public class RDFTransformation implements Transformation {

	private ObservationFactory factory;
	
	public RDFTransformation(ObservationFactory factory){
		this.factory = factory;
	}

	@Override
	public Observation transform(Observation fact,
			Consumer<Observation> generatedReceiver) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
