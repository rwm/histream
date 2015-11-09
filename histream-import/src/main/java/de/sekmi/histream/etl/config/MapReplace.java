package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Replace informations in facts.
 * Used as base class providing attributes to map case and map
 * 
 * @author R.W.Majeed
 *
 */
public abstract class MapReplace {
	@XmlAttribute(name="set-value")
	String setValue;
	
	@XmlAttribute(name="set-concept")
	String setConcept;
	
	@XmlAttribute(name="set-unit")
	String setUnit;

}
