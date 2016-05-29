package de.sekmi.histream.etl;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

public interface VisitLookup {

	/**
	 * Lookup a visit.
	 * @param patient patient to use to lookup the visit.
	 * @param localData local visit data
	 * @param localSource source for the visit data
	 * @return looked up visit
	 */
	public Visit lookupVisit(Patient patient, Visit localData, ExternalSourceType localSource);
	public void assignVisit(Observation fact, Visit visit);
}
