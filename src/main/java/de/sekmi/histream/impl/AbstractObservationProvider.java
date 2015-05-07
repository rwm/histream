package de.sekmi.histream.impl;

import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationProvider;

public abstract class AbstractObservationProvider implements ObservationProvider {
	private Consumer<Observation> consumer;
	
	public AbstractObservationProvider() {
	}
	
	@Override
	public void setHandler(Consumer<Observation> consumer) {
		this.consumer = consumer;
	}
	
	
	public void provideObservation(Observation observation){
		/*Iterator<ObservationHandler> iter = handlers.iterator();
		while( iter.hasNext() ){
			iter.next().accept(observation);
		}*/
		consumer.accept(observation);
	}

}
