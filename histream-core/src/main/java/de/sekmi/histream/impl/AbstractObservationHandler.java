package de.sekmi.histream.impl;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationHandler;

public abstract class AbstractObservationHandler implements ObservationHandler{
	private Consumer<ObservationException> errorHandler;
	private int errorCount;
	
	@Override
	public final void accept(Observation observation) {
		try {
			acceptOrException(observation);
		} catch (ObservationException e) {
			errorCount ++;
			e.setObservation(observation);
			reportError(e);
		} // don't catch runtime exceptions
	}
	
	/**
	 * Report observation exception
	 * @param e exception
	 */
	protected void reportError(ObservationException e){
		if( errorHandler != null )errorHandler.accept(e);
		else throw new RuntimeException("Exception encountered, no error handler", e);		
	}
	
	/**
	 * Accept method which allows exceptions. Exceptions are passed to the error handler
	 * specified via {@link #setErrorHandler(Consumer)}.
	 * @param observation observation
	 * @throws ObservationException for errors during the accept operation
	 */
	protected abstract void acceptOrException(Observation observation)throws ObservationException;

	@Override
	public void setErrorHandler(Consumer<ObservationException> handler) {
		this.errorHandler = handler;
	}

	/**
	 * Get the number of errors encountered
	 * @return error count
	 */
	public int getErrorCount(){ return errorCount;}
}
