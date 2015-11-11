package de.sekmi.histream.etl.validation;

import de.sekmi.histream.Observation;

/**
 * Validation encountered a duplicate visit.
 * <p>
 * The corresponding observation can be accessed via {@link #getObservation()}
 * 
 * @author R.W.Majeed
 *
 */
public class DuplicateVisitException extends ValidationException {

	private static final long serialVersionUID = 1L;
	
	public DuplicateVisitException(Observation fact) {
		super("Duplicate visit '"+fact.getEncounterId()+"' for patient '"+fact.getPatientId());
		setObservation(fact);
	}
}
