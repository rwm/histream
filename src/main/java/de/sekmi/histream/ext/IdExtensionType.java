package de.sekmi.histream.ext;

/**
 * Extension type which can be identified by a unique id
 * e.g. patient, visit, concept, etc.
 * @author Raphael
 *
 */
public interface IdExtensionType{
	String getId();
	
	/**
	 * Sets the id for the extension type instance.
	 * This method should be called only if the id was undefined before.
	 * The id should not be changed thereafter.
	 * @param id
	 */
	void setId(String id);
	
	@Override
	boolean equals(Object obj);
	
	@Override
	int hashCode();
}
