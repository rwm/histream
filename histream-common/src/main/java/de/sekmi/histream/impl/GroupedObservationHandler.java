package de.sekmi.histream.impl;

import java.util.Objects;
import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationHandler;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

/**
 * Handle observations which are grouped by patient and visit.
 * Abstract notification methods are provided if patient and visit changes between observations.
 * <p>
 * The callback methods are always called in the following order:
 * {@link #beginStream()}, {@link #beginPatient(Patient)}, {@link #beginEncounter(Visit)}
 * any number of {@link #onObservation(Observation)}, {@link #endEncounter(Visit)} 
 * optionally followed by the next encounter. {@link #endPatient(Patient)}.
 * <p>
 * Important: the final endEncounter, endPatient can only be called during close.
 * 
 * @author Raphael
 *
 */
public abstract class GroupedObservationHandler implements ObservationHandler, AutoCloseable {
	private Consumer<ObservationException> errorHandler;
	private Patient prevPatient;
	private Visit prevVisit;

	/**
	 * Called when the first observation is encountered
	 * @throws ObservationException to report errors
	 */
	protected abstract void beginStream()throws ObservationException;
	protected abstract void beginPatient(Patient patient)throws ObservationException;
	protected abstract void endPatient(Patient patient)throws ObservationException;
	protected abstract void beginEncounter(Visit visit)throws ObservationException;
	protected abstract void endEncounter(Visit visit)throws ObservationException;
	protected abstract void onObservation(Observation observation)throws ObservationException;
	
	protected abstract void endStream()throws ObservationException;
	
	@Override
	public void close(){
		if( prevVisit != null ){
			try {
				endEncounter(prevVisit);
			} catch (ObservationException e) {
				reportError(e);
			}
			prevVisit = null;
		}
		if( prevPatient != null ){
			try {
				endPatient(prevPatient);
			} catch (ObservationException e) {
				reportError(e);
			}
			prevPatient = null;
		}
		try {
			endStream();
		} catch (ObservationException e) {
			reportError(e);
		}
	}
	
	protected void reportError(ObservationException e){
		if( errorHandler != null )errorHandler.accept(e);
		else {
			throw new RuntimeException("Exception encountered, no error handler. Location="+e.getLocation()+", Fact="+e.getObservation(), e);
		}
	}
	
	@Override
	public void setErrorHandler(Consumer<ObservationException> handler) {
		this.errorHandler = handler;
	}
	
	@Override
	public final void accept(Observation observation) {
		Visit thisVisit = observation.getVisit();
		Patient thisPatient = thisVisit.getPatient();
		// assertations to simplify troubleshooting corrupt data
		Objects.requireNonNull(thisPatient);
		Objects.requireNonNull(thisPatient.getId(),"Patient w/o ID");
		Objects.requireNonNull(thisVisit);
		Objects.requireNonNull(thisVisit.getId(),"Visit w/o ID");

		if( prevPatient == null ){
			// write start document, meta, patient
			try {
				beginStream();
			} catch (ObservationException e) {
				e.setObservation(observation);
				reportError(e);
			}
			try {
				beginPatient(thisPatient);
			} catch (ObservationException e) {
				e.setObservation(observation);
				reportError(e);
			}

			prevPatient = thisPatient;
			prevVisit = null;
		}else if( !prevPatient.getId().equals(thisPatient.getId()) ){
			// close patient, write new patient
			try {
				endEncounter(prevVisit);
			} catch (ObservationException e) {
				e.setObservation(observation);
				reportError(e);
			}
			try {
				endPatient(prevPatient);
			} catch (ObservationException e) {
				e.setObservation(observation);
				reportError(e);
			}
			try {
				beginPatient(thisPatient);
			} catch (ObservationException e) {
				e.setObservation(observation);
				reportError(e);
			}

			prevPatient = thisPatient;
			prevVisit = null;
		}else{
			// same patient as previous fact
			// nothing to do
		}
		
		if( prevVisit == null ){
			// first visit for patient
			try {
				beginEncounter(thisVisit);
			} catch (ObservationException e) {
				e.setObservation(observation);
				reportError(e);
			}

			prevVisit = thisVisit;

		}else if( !prevVisit.getId().equals(thisVisit.getId()) ){
			try {
				endEncounter(prevVisit); // close previous encounter
			} catch (ObservationException e) {
				e.setObservation(observation);
				reportError(e);
			}
			try {
				beginEncounter(thisVisit);
			} catch (ObservationException e) {
				e.setObservation(observation);
				reportError(e);
			}
			prevVisit = thisVisit;

		}else{
			// same encounter as previous fact
			// nothing to do
		}
		
		// exceptions are passed on
		try {
			onObservation(observation);
		} catch (ObservationException e) {
			e.setObservation(observation);
			reportError(e);
		}
	}
}
