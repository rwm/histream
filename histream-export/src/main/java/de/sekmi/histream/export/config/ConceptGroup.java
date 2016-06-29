package de.sekmi.histream.export.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;

public class ConceptGroup {
	public ConceptGroup(String clazz){
		this.clazz = clazz;
		this.concepts = new ArrayList<>();
	}
	// constructor for JAXB
	protected ConceptGroup(){
	}
	
	@XmlID
	@XmlAttribute(name="class")
	String clazz;
	@XmlElement(name="concept")
	List<Concept> concepts;
}