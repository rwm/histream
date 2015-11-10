package de.sekmi.histream.etl.validation;

public class DuplicatePatientException extends ValidationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicatePatientException(String patientId) {
		super("Duplicate patient '"+patientId+"'");
	}

}
