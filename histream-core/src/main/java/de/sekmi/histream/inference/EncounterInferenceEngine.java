package de.sekmi.histream.inference;

import java.util.List;
import java.util.function.Consumer;

import de.sekmi.histream.Observation;

public interface EncounterInferenceEngine {

	String[] getRequiredConcepts();
	boolean useWildcardRequirements();
	void run(List<Observation> facts, Consumer<Observation> inferred);

	default void run(List<Observation> facts){
		run(facts, facts::add);
	}
}
