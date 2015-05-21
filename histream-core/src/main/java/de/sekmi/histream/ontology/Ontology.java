package de.sekmi.histream.ontology;

import java.io.Closeable;

/**
 * Ontology, loosely based on SKOS
 * 
 * @author Raphael
 *
 */
public interface Ontology extends Closeable{
	public Concept getConceptByNotation(String id) throws OntologyException;
	
	/**
	 * Return the top concepts of this ontology
	 * @return top concepts
	 * @throws OntologyException
	 */
	public Concept[] getTopConcepts() throws OntologyException;
	
	/**
	 * Returns the time when this ontology was last modified.
	 * @return A {@code long} value representing the time the file was last modified, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
	 */
	public long lastModified();
}
