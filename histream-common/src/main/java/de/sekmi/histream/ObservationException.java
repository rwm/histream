package de.sekmi.histream;

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


/**
 * Exception which occurred in connection with an observation
 * 
 * @author Raphael
 *
 */
public class ObservationException extends Exception{
	private static final long serialVersionUID = 1L;
	
	/** fact which was involved in the exception */
	private Observation fact;

	/** location indicator for the observation involved in the error.
	 * e.g. file and line number or XPath expression
	 */
	private String location;
	
	public ObservationException(String message){
		super(message);
	}
	public ObservationException(Throwable cause){
		super(cause);
	}
	public ObservationException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * Set the observation associated with this exception
	 * @param fact associated observation
	 */
	public void setObservation(Observation fact){
		this.fact = fact;
	}
	/**
	 * Set the observation associated with this exception
	 * @param fact associated observation
	 * @param location location e.g. line number or XPath expression
	 */
	public void setObservation(Observation fact, String location){
		this.fact = fact;
		this.location = location;
	}
	/**
	 * Get the observation associated with this exception.
	 * @return associated observation or null if none.
	 */
	public Observation getObservation(){return fact;}
	public String getLocation() {return location;}
}