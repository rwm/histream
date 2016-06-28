package de.sekmi.histream.inference;

public abstract class EncounterInferenceFactory extends InferenceFactory {
	
	/**
	 * Create an inference engine which infers the given concept id.
	 * @param inferredConceptId concept to infer
	 * @return inference engine or {@code null} if the concept is not
	 * 	supported by this factory.
	 */
	public abstract EncounterInferenceEngine createEngine(String inferredConceptId);
}
