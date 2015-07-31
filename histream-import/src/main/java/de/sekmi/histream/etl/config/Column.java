package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class Column {
	private Column(){
	}
	public Column(String name){
		this();
		this.name = name;
	}
	@XmlAttribute
	String na;
	
	@XmlAttribute(name="constant-value")
	String constantValue;
	
	@XmlValue
	String name;
}
