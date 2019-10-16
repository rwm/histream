package de.sekmi.histream.ontology;

import java.io.Closeable;

/**
 * Ontology, loosely based on SKOS
 * 
 * @author Raphael
 *
 */
public interface Ontology extends Closeable{
	/**
	 * Find a concept by it's notation. In SKOS terms, the id is equivalent to the concept's notation.
	 * @param notation concept id
	 * @return concept or {@code null} if not found
	 * @throws OntologyException for ontology errors
	 */
	public Concept getConceptByNotation(String notation) throws OntologyException;
	
	/**
	 * Find a concept by its ID / IRI
	 * @param id unique concept id
	 * @return concept or {@code null} if not found
	 * @throws OntologyException for ontology errors
	 */
	public Concept getConceptById(String id)throws OntologyException;
	
	/**
	 * Return the top concepts of this ontology
	 * @return top concepts
	 * @throws OntologyException for ontology errors
	 */
	public Concept[] getTopConcepts() throws OntologyException;
	/**
	 * Return the top concepts of this ontology in the specified scheme
	 * @param scheme scheme to which all returned top concepts belong 
	 * @return top concepts
	 * @throws OntologyException for ontology errors
	 */
	public Concept[] getTopConcepts(String scheme) throws OntologyException;
	
	/**
	 * Returns the time when this ontology was last modified.
	 * @return A {@code long} value representing the time the file was last modified, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
	 */
	public long lastModified();
}
