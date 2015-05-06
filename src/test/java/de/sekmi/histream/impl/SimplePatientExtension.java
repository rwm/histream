package de.sekmi.histream.impl;

import de.sekmi.histream.Extension;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.Patient;

public class SimplePatientExtension implements Extension<PatientImpl>{
	private final static Class<?>[] TYPES = new Class<?>[]{Patient.class, PatientImpl.class};
	
	@Override
	public Class<?>[] getInstanceTypes() {return TYPES;}

	@Override
	public PatientImpl createInstance() {return new PatientImpl();}

	@Override
	public PatientImpl createInstance(Observation observation) {
		PatientImpl patient = createInstance();
		patient.setId(observation.getPatientId());
		return patient;
	}

}
