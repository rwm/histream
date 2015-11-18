package de.sekmi.histream.etl.validation;

import de.sekmi.histream.Observation;

/**
 * Validation encountered a duplicate fact. A fact is regarded as duplicate,
 * if the concept is not marked repeatable and patient id, visit id, 
 * start date and visit id are equal.
 * 
 * <p>
 * The corresponding observation can be accessed via {@link #getObservation()}
 * 
 * @author R.W.Majeed
 *
 */
public class DuplicateConceptException extends ValidationException {

	private static final long serialVersionUID = 1L;
	
	public DuplicateConceptException(Observation fact) {
		super("Duplicate concept '"+fact.getConceptId()+"' for patient="+fact.getPatientId()+", visit="+fact.getEncounterId()+" at start="+fact.getStartTime());
		setObservation(fact);
	}
}
