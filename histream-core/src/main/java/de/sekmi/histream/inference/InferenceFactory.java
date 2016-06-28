package de.sekmi.histream.inference;

import java.util.Iterator;

import de.sekmi.histream.ObservationFactory;

public abstract class InferenceFactory {
	protected ObservationFactory factory;
	
	public void setObservationFactory(ObservationFactory factory){
		this.factory = factory;
	}
	/**
	 * Determine whether this inference factory can infer
	 * facts with the given concept id.
	 * 
	 * @param inferredConceptId inferred concept
	 * @return true if the concept can be inferred, false otherwise.
	 */
	public abstract boolean canInfer(String inferredConceptId);
	
	public abstract InferredConcept getConceptById(String inferredConceptId);
	
	/**
	 * Expand a list of concept IDs with dependencies of corresponding
	 * inferred concepts. 
	 * <p>
	 * If a concept can be inferred by this inference factory, it is 
	 * replaced with its dependencies. Other concept IDs are not modified.
	 * </p>
	 * <p>
	 * This is mainly useful for generating a list of concept ids for
	 * a database query.
	 * </p>
	 * @param concepts
	 * @return iterator of concepts which replaces inferred concepts
	 *  with their corresponding dependencies.
	 */
	public Iterator<String> expandConceptDependencies(Iterator<String> concepts){
		return new DependencyExpandingIterator(this, concepts);
	}
}
