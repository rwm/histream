package de.sekmi.histream.inference;

import de.sekmi.histream.ObservationFactory;

public interface EncounterInferenceFactory {

	void setObservationFactory(ObservationFactory factory);
	/**
	 * Determine whether this inference factory can infer
	 * facts with the given concept id.
	 * 
	 * @param inferredConceptId inferred concept
	 * @return true if the concept can be inferred, false otherwise.
	 */
	boolean canInfer(String inferredConceptId);
	
	/**
	 * Create an inference engine which infers the given concept id.
	 * @param inferredConceptId concept to infer
	 * @return inference engine or {@code null} if the concept is not
	 * 	supported by this factory.
	 */
	public EncounterInferenceEngine createEngine(String inferredConceptId);
}
