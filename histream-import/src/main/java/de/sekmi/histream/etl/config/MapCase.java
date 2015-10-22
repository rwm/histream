package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class MapCase {
	@XmlAttribute
	String value;
	
	@XmlAttribute(name="set-value")
	String setValue;
	
	@XmlAttribute(name="set-concept")
	String setConcept;
	
	// TODO use enum
	@XmlAttribute
	String action;
	
	@XmlAttribute(name="log-warning")
	String logWarning;
	
}