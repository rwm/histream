package de.sekmi.histream;

public interface ExtensionAccessor<T> {
	/**
	 * Get the extension type instance. The instance is created automatically on first access.
	 * @param observation
	 * @return
	 */
	T access(Observation observation);
	
	void set(Observation observation, T ext);
	// TODO: if necessary, create method isAvailable which does not create the instance automatically
}
