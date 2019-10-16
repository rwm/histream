package de.sekmi.histream.impl;

import javax.xml.bind.annotation.XmlAttribute;

public class ScopedProperty {

	@XmlAttribute
	String path;
	
	@XmlAttribute
	String name;

	@XmlAttribute
	String value;

	/**
	 * Empty constructor for JAXB
	 */
	protected ScopedProperty() {
		
	}
	public ScopedProperty(String path, String name, String value) {
		this.path = path;
		this.name = name;
		this.value = value;
	}
}
