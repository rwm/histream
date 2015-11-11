package de.sekmi.histream.io.transform;

import java.util.function.Consumer;
import java.util.function.Function;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

/**
 * Replace patient and visit information for each observation.
 * 
 * @author Raphael
 *
 */
public class PatientVisitTransformation implements Transformation {

	private Function<String, String> patientIdMapper;
	private Function<String, String> visitIdMapper;
	private Function<Patient, Patient> patientMapper;
	private Function<Visit, Visit> visitMapper;
	
	
	public PatientVisitTransformation() {
	}
	
	/**
	 * Set the patient mapping functions
	 * 
	 * @param patientExtensionMapper map a patient. 
	 * Warning, changing the patient object might also change the patient object
	 * of other facts which share the patient extension. If you do so, take care
	 * not to process the same patient object again.
	 * 
	 * @param patientIdMapper maps the patient id from {@link Observation#getPatientId()}. 
	 * This function is always called after the patientExtensionMapper.
	 */
	public void setPatientMapping(Function<Patient,Patient> patientExtensionMapper, Function<String,String> patientIdMapper){
		this.patientIdMapper = patientIdMapper;
		this.patientMapper = patientExtensionMapper;
	}
	public void setVisitMapping(Function<Visit,Visit> visitExtensionMapper, Function<String,String> visitIdMapper){
		this.visitIdMapper = visitIdMapper;
		this.visitMapper = visitExtensionMapper;
	}
	@Override
	public Observation transform(Observation fact, Consumer<Observation> generatedReceiver)
			throws TransformationException {
		
		if( patientMapper != null ){
			fact.setExtension(Patient.class, patientMapper.apply(fact.getExtension(Patient.class)));
		}
		if( patientIdMapper != null ){
			fact.setPatientId(patientIdMapper.apply(fact.getPatientId()));
		}
		if( visitMapper != null ){
			fact.setExtension(Visit.class, visitMapper.apply(fact.getExtension(Visit.class)));
		}
		if( visitIdMapper != null ){
			fact.setEncounterId(visitIdMapper.apply(fact.getEncounterId()));
		}

		return fact;
	}

}
