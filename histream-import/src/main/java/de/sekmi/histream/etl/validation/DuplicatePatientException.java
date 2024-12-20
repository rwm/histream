package de.sekmi.histream.etl.validation;

import de.sekmi.histream.Observation;

/**
 * Validation encountered a duplicate patient.
 * <p>
 * The corresponding observation can be accessed via {@link #getObservation()}
 * 
 * @author R.W.Majeed
 *
 */
public class DuplicatePatientException extends ValidationException {

	private static final long serialVersionUID = 1L;
	
	public DuplicatePatientException(Observation fact) {
		super("Duplicate patient '"+fact.getPatientId()+"'");
		setObservation(fact);
	}
}
