package de.sekmi.histream.etl.validation;


import java.util.HashSet;
import java.util.Set;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.impl.AbstractObservationHandler;

public class Validator extends AbstractObservationHandler{

	private String prevPatient;
	private String prevVisit;
	private Set<String> patients;
	private Set<String> visits;
	private Set<StartPlusConcept> concepts;

	private boolean duplicateVisitCheck;
	private boolean duplicateConceptCheck;
	
	public Validator(){
		patients = new HashSet<>();
		visits = new HashSet<>();
		concepts = new HashSet<>();
		this.duplicateVisitCheck = true;
		this.duplicateConceptCheck = true;
	}

	@Override
	protected void acceptOrException(Observation t) throws ObservationException {
		String patid = t.getPatientId();
		String encid = t.getEncounterId();

		if( prevPatient == null || !prevPatient.equals(patid) ){
			// check if patient already known
	
			// clear visit
			visits.clear();
			prevVisit = encid;
			visits.add(encid);

			prevPatient = patid; // remember patient to suppress errors for the same patient

			if( patients.contains(patid) ){
				throw new DuplicatePatientException(patid);
			}else{
				patients.add(patid);
			}			
		}else{
			// patient already known. 
			// check if encounter already known
			if( duplicateVisitCheck && !prevVisit.equals(encid) ){
				prevVisit = encid; // remember encounter to suppress errors for the same encounter

				if( visits.contains(encid) ){
					throw new ValidationException("Duplicate encounter '"+encid+"' for patient '"+patid+"'");
				}else{
					visits.add(encid);
				}
			}

			// check for duplicate non-repeating start+concept tuples
			if( duplicateConceptCheck ){
				StartPlusConcept spc = new StartPlusConcept(t);
				if( concepts.contains(spc) ){
					throw new ValidationException("Duplicate concept: patid="+patid+", visit="+encid+", concept="+t.getConceptId()+", start="+t.getStartTime());
				}else{
					concepts.add(spc);
				}
			}
		}
		
	}

	@Override
	public void setMeta(String key, String value) {
		// TODO Auto-generated method stub
		
	}

}
