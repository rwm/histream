package de.sekmi.histream.impl;

import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationHandler;

public abstract class AbstractObservationHandler implements ObservationHandler{
	Consumer<ObservationException> errorHandler;
	
	@Override
	public final void accept(Observation observation) {
		try {
			acceptOrException(observation);
		} catch (ObservationException e) {
			if( errorHandler != null )errorHandler.accept(e);
			else throw new RuntimeException("Exception encountered, no error handler", e);
		} // don't catch runtime exceptions
	}
	
	/**
	 * Accept method which allows exceptions. Exceptions are passed to the error handler
	 * specified via {@link #setErrorHandler(Consumer)}.
	 * @param observation
	 * @throws ObservationException
	 */
	protected abstract void acceptOrException(Observation observation)throws ObservationException;

	@Override
	public void setErrorHandler(Consumer<ObservationException> handler) {
		this.errorHandler = handler;
	}

}
