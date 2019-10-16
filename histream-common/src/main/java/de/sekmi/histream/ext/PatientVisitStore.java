package de.sekmi.histream.ext;

import java.util.List;

import de.sekmi.histream.DateTimeAccuracy;

public interface PatientVisitStore {
	Patient findPatient(String patientId);
	Visit findVisit(String visitId);
	void merge(Patient patient, String additionalId, ExternalSourceType source);

	Patient createPatient(String patientId, ExternalSourceType source);
	Visit createVisit(String visitId, DateTimeAccuracy start, Patient patient, ExternalSourceType source);
	/**
	 * Get alias ids for the given patient (e.g. resulting from a merge) 
	 * @param patient patient instance
	 * @return alias ids
	 */
	String[] getPatientAliasIds(Patient patient);
	
	/**
	 * Deletes the patient identified by given id. This method does not remove any other associated
	 * data e.g. like visits, observations.
	 * @param id patient id
	 */
	void purgePatient(String patientId);
	void purgeVisit(String visitId);

	List<? extends Visit> allVisits(Patient patient);
}
