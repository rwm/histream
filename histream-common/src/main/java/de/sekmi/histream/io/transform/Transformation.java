package de.sekmi.histream.io.transform;

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
	 * @param generatedReceiver receiver for facts generated by the transformation
	 * @return new/original transformed observation or null if it should be removed
	 * @throws TransformationException transformation error
	 */
	Observation transform(Observation fact, Consumer<Observation> generatedReceiver) throws TransformationException;
	
	public static final Transformation Identity = new Transformation(){

		@Override
		public Observation transform(Observation fact,
				Consumer<Observation> generatedReceiver) {
			return fact;
		}
		
	};
}
