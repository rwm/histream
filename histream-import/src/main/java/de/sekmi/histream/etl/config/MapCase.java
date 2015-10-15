package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class MapCase {
	String value;
	
	@XmlElement(name="set-value")
	String setValue;
	
	@XmlElement(name="set-concept")
	String setConcept;
	
	// TODO use enum
	String action;
	
	@XmlElement(name="log-warning")
	String logWarning;
	
}
