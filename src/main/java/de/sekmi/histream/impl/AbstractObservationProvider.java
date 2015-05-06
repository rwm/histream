package de.sekmi.histream.impl;

import java.util.Iterator;
import java.util.LinkedList;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationHandler;
import de.sekmi.histream.ObservationProvider;

public abstract class AbstractObservationProvider implements ObservationProvider {
	private LinkedList<ObservationHandler> handlers;
	
	public AbstractObservationProvider() {
		handlers = new LinkedList<>();
	}
	
	@Override
	public void addHandler(ObservationHandler handler) {
		handlers.add(handler);
	}

	@Override
	public void removeHandler(ObservationHandler handler) {
		handlers.remove(handler);
	}
	
	
	public void provideObservation(Observation observation){
		Iterator<ObservationHandler> iter = handlers.iterator();
		while( iter.hasNext() ){
			iter.next().accept(observation);
		}
	}

}
