package de.sekmi.histream.io;

import java.util.function.Consumer;

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
	 * The original observation might be changed, removed (if null is returned) or added 
	 * (passed to the generatedReceiver).
	 * @param fact observation to transform
	 * @return new transformed observation or empty optional if it should be removed
	 */
	Observation transform(Observation fact, Consumer<Observation> generatedReceiver);
	
	public static final Transformation Identity = new Transformation(){

		@Override
		public Observation transform(Observation fact,
				Consumer<Observation> generatedReceiver) {
			return fact;
		}
		
	};
}
