package de.sekmi.histream.etl.validation;

import de.sekmi.histream.ObservationException;

public class ValidationException extends ObservationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValidationException(String message) {
		super(message);
	}

}
