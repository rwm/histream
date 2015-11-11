package de.sekmi.histream.io.transform;

import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationHandler;

/**
 * Use {@link ObservationHandler} in {@link Transformation}.
 * Bridge between {@link ObservationHandler} and {@link Transformation}.
 * 
 * @author R.W.Majeed
 *
 */
public class ReadOnlyTransformation implements Transformation {
	private ObservationHandler handler;
	
	public ReadOnlyTransformation(ObservationHandler handler){
		this.handler = handler;
	}
	
	@Override
	public Observation transform(Observation fact, Consumer<Observation> generatedReceiver) {
		handler.accept(fact);
		return fact;
	}

}
