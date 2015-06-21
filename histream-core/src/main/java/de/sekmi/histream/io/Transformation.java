package de.sekmi.histream.io;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import de.sekmi.histream.Observation;

/**
 * Transformation of observations.
 * <p>
 * A transformation can modify or remove observations.
 * @author Raphael
 *
 */
public interface Transformation {

	/**
	 * Transforms a single observation. 
	 * The original observation is not changed. 
	 * @param fact observation to transform
	 * @return new transformed observation or empty opional if it should be removed
	 */
	Optional<Observation> transform(Observation fact);
	
	/**
	 * Returns whether this transformation wants the supplied observation to be removed.
	 * @param fact observation to check for removal
	 * @return true if the observation should be removed, false to keep the observation
	 */
	boolean wantRemoved(Observation fact);
	
	
	/**
	 * Modifies the supplied observation to match the transformation rule.
	 * @param fact observation to modify (in place)
	 * @return whether the supplied observation was modified.
	 */
	boolean modify(Observation fact);
	
	
	/**
	 * Returns a function which applies this transformations 
	 * modifications to any observation. Observations will not
	 * be removed by the returned function.
	 * 
	 * @return function which applies this transformation
	 */
	Function<Observation, Observation> modificationFunction();
	
	/**
	 * Returns a predicate which indicates whether to allow the
	 * observation.
	 * <p>
	 * The predicate's {@link Predicate#test(Object)} function will 
	 * true if the argument should be allowed or modified, and false 
	 * if the argument should be removed.
	 * @return predicate indicating whether to allow or remove an observation
	 */
	Predicate<Observation> allowedPredicate();
	
}
