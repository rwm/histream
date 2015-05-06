package de.sekmi.histream;


/**
 * Single instance which generates all observations. 
 * Manages extensions which enhance/annotate observations.
 * 
 * @author marap1
 *
 */
public interface ObservationFactory {
	/**
	 * Register an extension. Registered extensions cannot be removed.
	 * @param extension
	 */
	<T> void registerExtension(Extension<T> extension);
	
	/**
	 * Get a list of currently registered extensions
	 * @return currently registered extensions
	 */
	//Iterable<Extension<?>> registeredExtensions();
	
	/**
	 * Extensions can be accessed through either via {@link Observation#getExtension(Class)}
	 * or via an {@link ExtensionAccessor}. The latter method is faster, because no hash
	 * lookup of the extensionType needs to be performed.
	 * 
	 * @param extensionType type to get an accessor for
	 * @return extension accessor or null if the extension is not available
	 */
	<T> ExtensionAccessor<T> getExtensionAccessor(Class<T> extensionType);
	
	/**
	 * Create a new observation
	 * @param patientId
	 * @param conceptId
	 * @param startTime TODO
	 * @return
	 */
	Observation createObservation(String patientId, String conceptId, DateTimeAccuracy startTime);
}
