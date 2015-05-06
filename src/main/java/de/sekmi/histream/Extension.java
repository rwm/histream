package de.sekmi.histream;


/**
 * Extensions allow additional information to be stored and retrieved
 * for observations. 
 * 
 * @author Raphael
 *
 * @param <T>
 */
public interface Extension<T>{
	/**
	 * Creates a new instance for the given observation. This is only called
	 * once for each observation, usually when the extension requested for
	 * the observation. The instance is then cached automatically.
	 * 
	 * @param observation
	 * @return extension
	 */
	T createInstance(Observation observation);
	
	/**
	 * Creates a static instance which is independent of a given observation.
	 * <p>
	 * Some extensions do not support independent instances. In this case, all
	 * calls to this method will result in an {@link UnsupportedOperationException}.
	 * @return new instance 
	 * @throws UnsupportedOperationException if instance creation without {@link Observation} is not possible.
	 */
	T createInstance() throws UnsupportedOperationException;
	
	/**
	 * Get class of the instance type. Should be a basic interface like Patient, Visit, Location, Concept, etc.
	 * TODO change return type to array, to register all compatible classes
	 * @return instance type
	 */
	Class<?>[] getInstanceTypes();
	
}
