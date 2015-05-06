package de.sekmi.histream.ext;


/**
 * Interface for patient store. Can be used to manage patients (e.g. merge)
 * TODO add classes for measuring performance for random access of patient store / visit store / concept store
 * @author Raphael
 *
 */
public interface PatientStore {
	Patient retrieve(String id);
	void merge(Patient patient, String additionalId, ExternalSourceType source);
	
	/**
	 * Get alias ids for the given patient (e.g. resulting from a merge) 
	 * @param patient
	 * @return
	 */
	String[] getAliasIds(Patient patient);
	
	/**
	 * Deletes the patient identified by given id. This method does not remove any other associated
	 * data e.g. like visits, observations.
	 * @param id
	 */
	void purge(String id);
}
