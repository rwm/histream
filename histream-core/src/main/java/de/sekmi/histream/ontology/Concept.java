package de.sekmi.histream.ontology;

import java.util.Locale;

/**
 * Ontology concept, loosely based on SKOS concept
 * 
 * @author marap1
 *
 */
public interface Concept {
	/**
	 * Get concepts which are in a narrower relationship to this concept.
	 * The narrower property is not transitive. Broader and narrower are inverse relationships.
	 * 
	 * @return narrower concepts. May return an empty array if there are no narrower concepts.
	 * @throws OntologyException for ontology errors. 
	 * @see #getBroader()
	 */
	Concept[] getNarrower() throws OntologyException;
	/**
	 * Get concepts which are in a broader relationship to this concept.
	 * The broader property is not transitive. Broader and narrower are inverse relationships.
	 * 
	 * @return broader concepts. May return an empty array if there are no broader concepts.
	 * @throws OntologyException for ontology errors.
	 * @see #getNarrower()
	 */
	Concept[] getBroader() throws OntologyException;
	
	/**
	 * Get concept id's. A can have multiple id's assigned. 
	 * 
	 * SKOS allows multiple notations for a single concept, 
	 * but there should be no two concepts sharing the same notation.
	 * <p>
	 * TODO i2b2 appears to support multiple notation via the M attribute (instead of L)
	 * 
	 * @return String array containing all id's assigned to this concept
	 * @throws OntologyException for ontology errors
	 */
	String[] getIDs() throws OntologyException;
	
	/**
	 * Get the concept's preferred label for a given locale.
	 * 
	 * Specify {@link Locale#ROOT} or null to access a language neutral
	 * label (which doesn't have a language specified).
	 * 
	 * @param locale locale for the label to receive.
	 * @return locale string or {@code null} if there is no label for the specified language.
	 * @throws OntologyException for ontology errors
	 */
	String getPrefLabel(Locale locale) throws OntologyException;
	
	/**
	 * Get the concept's description for a given locale.
	 * See also {link #getPrefLabel(Locale)}
	 * 
	 * @param locale locale for the description to receive
	 * @return description or null if no description for the specified language
	 * @throws OntologyException for ontology errors
	 */
	String getDescription(Locale locale) throws OntologyException;
	
	/**
	 * Get concepts which are part of this concepts. 
	 * <p>
	 * The part relationship is different from the narrower relationship
	 * in the sense that {@link #getNarrower()} will point to more specific concepts while
	 * {@link #getParts()} will divide the concept in the sense of a whole/part relationship.
	 * 
	 * @return concepts which are part of this concept, or {@code null} if this concept cannot be divided.
	 */
	Concept[] getParts();
	
	/**
	 * Get restrictions for the values of this concept.
	 * @return value restrictions or {@code null} if no restrictions are provided
	 */
	ValueRestriction getValueRestriction();
}
