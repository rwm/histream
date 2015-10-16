package de.sekmi.histream.etl.config;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.etl.ParseException;

/**
 * Date and Time column.
 * TODO implement parsing of partial date/time. e.g. 2003-10
 * 
 * @author Raphael
 *
 */
public class DateTimeColumn extends Column<DateTimeAccuracy>{
	@XmlTransient
	DateTimeFormatter formatter;
	/**
	 * Format string for parsing via {@link DateTimeFormatter}
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
	
	@Override
	public DateTimeAccuracy valueOf(Object value) throws ParseException{
		if( value instanceof Timestamp ){
			// convert from timestamp
			return null;
		}else{
			throw new IllegalArgumentException("Don't know how to parse type "+value.getClass()+" to datetime");
		}
	}

	@Override
	public DateTimeAccuracy valueFromString(String input) throws ParseException {
		// parse date according to format
		if( formatter == null && format != null ){
			formatter = DateTimeFormatter.ofPattern(format);
		}
		if( formatter == null ){
			throw new ParseException("format must be specified for DateTime fields if strings are parsed");
		}
		// parse
		try{
			return DateTimeAccuracy.parse(formatter,input);
		}catch( DateTimeParseException e ){
			throw new ParseException("Unable to parse date: "+input, e);
		}
	}
}