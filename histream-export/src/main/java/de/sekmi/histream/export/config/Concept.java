package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class Concept {
	@XmlAttribute
	String iri;
	@XmlAttribute
	String notation;
	@XmlAttribute(name="wildcard-notation")
	String wildcardNotation;
	
	public static Concept newWildcard(String wildcardNotation){
		Concept c = new Concept();
		c.wildcardNotation = wildcardNotation;
		return c;
	}
	public static Concept newIRI(String iri){
		Concept c = new Concept();
		c.iri = iri;
		return c;	
	}
	public static Concept newNotation(String notation){
		Concept c = new Concept();
		c.notation = notation;
		return c;	
	}
	public String getIRI(){
		return iri;
	}
	public String getNotation(){
		return notation;
	}
	public String getWildcardNotation(){
		return wildcardNotation;
	}
}
