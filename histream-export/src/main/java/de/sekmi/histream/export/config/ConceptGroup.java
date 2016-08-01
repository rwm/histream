package de.sekmi.histream.export.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;

/**
 * Group containing concepts. Useful for grouping
 * concepts under a common 'class' name, which can
 * be used to identify facts later without
 * the need to refer to their (different) concept
 * codes.
 * @author R.W.Majeed
 *
 */
public class ConceptGroup {
	public ConceptGroup(String clazz){
		this.clazz = clazz;
		this.concepts = new ArrayList<>();
	}
	// constructor for JAXB
	protected ConceptGroup(){
	}
	
	@XmlID
	@XmlAttribute(name="class", required=true)
	String clazz;
	@XmlElement(name="concept")
	List<Concept> concepts;
	
	/**
	 * Get the class (id) attribute for this group.
	 * @return group class
	 */
	public String getClazz(){
		return clazz;
	}
	/**
	 * Get the concepts in this group
	 * @return concepts
	 */
	public Iterable<Concept> getConcepts(){
		return concepts;
	}
}