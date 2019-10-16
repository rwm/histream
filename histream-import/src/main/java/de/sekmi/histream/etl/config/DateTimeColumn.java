package de.sekmi.histream.etl.config;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.IllformedLocaleException;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
	 * Format string for parsing via {@link DateTimeFormatter}.
	 * For locale specific formats, the locale can be specified
	 * via {@link #locale}.
	 * <p>
	 * If no locale is specified, the parsing
	 * is done via {@link DateTimeFormatter#ofPattern(String)} which 
	 * uses the system default locale.
	 * </p>
	 */
	@XmlAttribute
	String format;

	/** Locale to use for parsing date strings. Specified as BCP 47 tag. 
	 * Some formats support naming months e.g. Mar 1st 2018. 
	 * In these cases, a locale must be specified to indicate the language 
	 * for month names etc.
	 * See {@link DateTimeFormatter#ofPattern(String, java.util.Locale)}
	 * <p>
	 * If no locale specified, the java default locale is used.
	 * See {@link DateTimeFormatter#ofPattern(String)}
	 * </p>
	 * */
	@XmlAttribute
	String locale;

	@XmlAttribute
	String zone;
	
	/** attribute to reduce accuracy for privacy reasons. 
	 * e.g. output-resolution=Years.
	 */
	@XmlAttribute(name="target-precision")
	@XmlJavaTypeAdapter(ChronoPrecisionAdapter.class)
	ChronoUnit targetPrecision;
	/**
	 * Construct a date time column
	 * @param name column name
	 * @param format date format
	 * @param locale locale, set to {@code null} for java default locale
	 */
	public DateTimeColumn(String name, String format, String locale){
		super(name);
		this.format = format;
		this.locale = locale;
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
			if( locale == null ) {
				formatter = DateTimeFormatter.ofPattern(format);
			}else try {
				formatter = DateTimeFormatter.ofPattern(format, Locale.forLanguageTag(locale));
			}catch( IllformedLocaleException e ) {
				throw new ParseException("Failed to parse DateTime column locale tag: "+locale);
			}
		}
		if( formatter == null ){
			throw new ParseException("format must be specified for DateTime fields if strings are parsed");
		}
		ZoneId zoneId;
		if( zone != null ){
			zoneId = ZoneId.of(zone);
		}else{
			zoneId = ZoneId.systemDefault();
		}
		// parse
		try{
			return DateTimeAccuracy.parse(formatter,input, zoneId);
		}catch( DateTimeParseException e ){
			throw new ParseException("Unable to parse date '"+input+"' in column '"+this.column+"'", e);
		}
	}

	public static final DateTimeColumn alwaysNull(){
		DateTimeColumn c = new DateTimeColumn(){
			@Override
			public DateTimeAccuracy valueFromString(String input){return null;}
			@Override
			public DateTimeAccuracy valueOf(Object input){return null;}
		};
		c.constantValue = "";
		c.na = "";
		return c;
	}
}