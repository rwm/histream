package de.sekmi.histream.etl.config;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class Script {

	/** 
	 * Character encoding for an external script file 
	 */
	@XmlAttribute
	String charset;
	
	/** 
	 * Media type for the script (optional). {@code text/javascript} is the default value.
	 */
	@XmlAttribute
	String type;
	
	/**
	 * Specifies the URL of an external script file. The URL can be relative
	 * to the configuration.
	 */
	@XmlAttribute
	String src;
	
	@XmlValue
	public String content;
}
