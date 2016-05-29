
package de.sekmi.histream.etl;

import java.util.Objects;

import de.sekmi.histream.ExtensionAccessor;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

/**
 * Synchronize patient and visit data with extensions
 * 
 * @author R.W.Majeed
 *
 */
class ExtensionSync implements VisitLookup, PatientLookup{

	private ExtensionAccessor<Patient> patientAccessor;
	private ExtensionAccessor<Visit> visitAccessor;
	
	public ExtensionSync(ObservationFactory factory){
		patientAccessor = factory.getExtensionAccessor(Patient.class);
		Objects.requireNonNull(patientAccessor);
		visitAccessor = factory.getExtensionAccessor(Visit.class);
		Objects.requireNonNull(visitAccessor);
	}

	@Override
	public Visit lookupVisit(Patient patient, Visit localData, ExternalSourceType localSource) {
		Visit instance = visitAccessor.accessStatic(localData.getId(), patient, localSource);
		instance.setStartTime(localData.getStartTime());
		instance.setEndTime(localData.getEndTime());
		instance.setLocationId(localData.getLocationId());
		instance.setStatus(localData.getStatus());
		return instance;
	}

	@Override
	public Patient lookupPatient(Patient localData, ExternalSourceType localSource) {
		Patient instance = patientAccessor.accessStatic(localData.getId()/*.getPatientId()*/, localSource);
		instance.setBirthDate(localData.getBirthDate());
		instance.setDeathDate(localData.getDeathDate());
		instance.setSex(localData.getSex());
		instance.setSurname(localData.getSurname());
		instance.setGivenName(localData.getGivenName());
		// TODO sync patient with extension factory / add fields
		return instance;

	}

	@Override
	public void assignPatient(Observation fact, Patient patient) {
		patientAccessor.set(fact, patient);
	}

	@Override
	public void assignVisit(Observation fact, Visit visit) {
		visitAccessor.set(fact, visit);
	}
	
}
