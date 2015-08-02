package de.sekmi.histream.etl.config;

import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Date and Time column.
 * TODO implement parsing of partial date/time. e.g. 2003-10
 * 
 * @author Raphael
 *
 */
public class DateTimeColumn extends Column{
	@XmlTransient
	DateTimeFormatter formatter;
	/**
	 * Decimal format string for parsing via {@link DateTimeFormatter}
	 * @see DateTimeFormatter#ofPattern(String)
	 */
	@XmlAttribute
	String format;
	
	
	public DateTimeColumn(String name, String format){
		super(name);
		this.format = format;
	}
	
	protected DateTimeColumn(){
		super();
	}
}