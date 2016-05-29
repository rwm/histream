package de.sekmi.histream.etl;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;

public interface PatientLookup {

	public Patient lookupPatient(Patient localData, ExternalSourceType localSource);
	public void assignPatient(Observation fact, Patient patient);
	
}
