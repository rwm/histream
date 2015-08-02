package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Concept from a wide table
 * @author Raphael
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Concept{
	@XmlAttribute(required=true)
	String id;
	// TODO: value should contain also type (string,decimal,integer,...)
	Column value;
	StringColumn unit;
	@XmlElement(required=true)
	DateTimeColumn start;
	DateTimeColumn end;
	@XmlElement(name="modifier")
	Modifier[] modifiers;
	// ...
	
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Modifier{
		@XmlAttribute(required=true)
		String id;
		// TODO: value with type
		Column value;
		StringColumn unit;
		
		private Modifier(){
		}
		public Modifier(String id){
			this();
			this.id = id;
		}
	}
	
	private Concept(){
	}
	
	public Concept(String id, String startColumn, String format){
		this();
		this.id = id;
		this.start = new DateTimeColumn(startColumn, format);
	}
	
}