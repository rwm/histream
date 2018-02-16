package de.sekmi.histream.etl.validation;


import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.impl.AbstractObservationHandler;
import de.sekmi.histream.io.transform.Transformation;
import de.sekmi.histream.io.transform.TransformationException;

/**
 * Validates facts coming from a grouped patient stream.
 * 
 * @author R.W.Majeed
 *
 */
public class Validator extends AbstractObservationHandler implements Transformation{

	private String prevPatient;
	private String prevVisit;
	private Set<String> patients;
	private Set<String> visits;
	private Set<StartPlusConcept> concepts;

	private Consumer<Observation> droppedConceptHandler;
	
	private boolean duplicateVisitCheck;
	private boolean duplicateConceptCheck;
	
	public Validator(boolean duplicateVisitCheck, boolean duplicateConceptCheck){
		patients = new HashSet<>();
		visits = new HashSet<>();
		concepts = new HashSet<>();
		this.duplicateVisitCheck = duplicateVisitCheck;
		this.duplicateConceptCheck = duplicateConceptCheck;
	}
	
	/**
	 * Configure the validator to drop duplicate concepts. 
	 * Does only work if the validator is used as {@link Transformation}.
	 * 
	 * @param droppedConceptAction action to perform for dropped concepts
	 * @throws NullPointerException if the argument is null
	 * @throws UnsupportedOperationException if the validator was configured not to validate concepts
	 */
	public void dropDuplicateConcepts(Consumer<Observation> droppedConceptAction)throws NullPointerException, UnsupportedOperationException{
		if( false == duplicateConceptCheck ){
			throw new IllegalArgumentException("need duplicateConceptCheck to dropDuplicateConcepts");
		}		
		this.droppedConceptHandler = droppedConceptAction;
	}

	/**
	 * Validate facts in a grouped patient stream
	 * @param t fact to validate
	 * @throws DuplicatePatientException duplicate patient outside of grouped patient
	 * @throws DuplicateVisitException duplicate visit outside of grouped visit
	 * @throws DuplicateConceptException duplicate concept id for same visit and timestamp
	 */
	public void validateFact(Observation t)throws DuplicatePatientException, DuplicateVisitException, DuplicateConceptException{
		String patid = t.getPatientId();
		String encid = t.getEncounterId();

		// check if patient already known
		if( prevPatient == null || !prevPatient.equals(patid) ){
			// new patient

			// clear visit
			visits.clear();
			prevVisit = encid;
			visits.add(encid);

			// clear concepts
			concepts.clear();
			// add concept
			if( duplicateConceptCheck ){
				concepts.add(new StartPlusConcept(t));
			}
			
			prevPatient = patid; // remember patient to suppress errors for the same patient

			if( patients.contains(patid) ){
				throw new DuplicatePatientException(t);
			}else{
				patients.add(patid);
			}			
		}else{
			// patient already known. 
			// check if encounter already known
			if( duplicateVisitCheck && !prevVisit.equals(encid) ){
				prevVisit = encid; // remember encounter to suppress errors for the same encounter

				if( visits.contains(encid) ){
					throw new DuplicateVisitException(t);
				}else{
					visits.add(encid);
				}
				
				// clear concepts
				concepts.clear();
			}

			// check for duplicate non-repeating start+concept tuples
			if( duplicateConceptCheck ){
				StartPlusConcept spc = new StartPlusConcept(t);
				if( concepts.contains(spc) ){
					throw new DuplicateConceptException(t);
				}else{
					concepts.add(spc);
				}
			}
		}
	}
	@Override
	protected void acceptOrException(Observation t) throws ObservationException {
		validateFact(t);
	}

	@Override
	public void setMeta(String key, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Observation transform(Observation fact, Consumer<Observation> generatedReceiver)
			throws TransformationException {
		try {
			validateFact(fact);
		} catch (DuplicateConceptException e ){
			// ignore duplicate concepts?
			if( droppedConceptHandler != null ){
				// callback to report ignored fact
				droppedConceptHandler.accept(fact);
				return null;
			}else{
				reportError(e);
			}
		} catch (DuplicatePatientException | DuplicateVisitException e) {
			reportError(e);
		}
		return fact;
	}

}
