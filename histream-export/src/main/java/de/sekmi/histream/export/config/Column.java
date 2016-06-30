package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class Column {

	/**
	 * Column header name
	 */
	@XmlAttribute(required=true)
	String header;


	/**
	 * Column value to use to indicate NA.
	 * This will be used if null values are
	 * encountered by the column. If not specified,
	 * the empty string {@code ''} will be used.
	 */
	@XmlAttribute
	String na;
	
	/**
	 * XPath expression to select the column's value
	 */
	@XmlAttribute(required=true)
	String xpath;
	
	public Column(String header, String xpath){
		this.header = header;
		this.xpath = xpath;
	}
	// constructor for JAXB
	protected Column(){
	}
	
	public String getXPath(){
		return xpath;
	}
	public String getHeader(){
		return header;
	}
}
