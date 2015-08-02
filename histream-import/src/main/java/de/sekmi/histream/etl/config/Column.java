package de.sekmi.histream.etl.config;

import java.text.DecimalFormat;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlTransient
@XmlSeeAlso({StringColumn.class})
public class Column {
	protected Column(){
	}
	public Column(String name){
		this();
		this.name = name;
	}
	/**
	 * If this string is found in the column data, the resulting value will be null.
	 */
	@XmlAttribute
	String na;
	
	/**
	 * If set (e.g. non-null), this will always overwrite any other value. 
	 * Datatype formats and regular expressions are still applied to the constant value.
	 */
	@XmlAttribute(name="constant-value")
	String constantValue;
	
	/**
	 * Regular expression which needs to match the input string
	 */
	@XmlAttribute(name="regex-match")
	String regexMatch;
	
	/**
	 * Replace the input value with the specified string or regular expression group from {@link #regexMatch}.
	 * If not specified, the full input string is used (regardless of match region).
	 */
	@XmlAttribute(name="regex-replace")
	String regexReplace;
	
	/**
	 * Action to perform if the {@link #regexMatch} did not match the input string.
	 * Either use NA (usually null) for the value, or drop the whole concept/fact.
	 */
	@XmlAttribute(name="regex-nomatch-action")
	String regexNoMatchAction; // either na or drop
	
	/**
	 * Report a warning if the {@link #regexMatch} did not match the input string.
	 * Defaults to true.
	 */
	@XmlAttribute(name="regex-nomatch-warning")
	Boolean regexNoMatchWarning;
	
	/**
	 * Column name to use for reading input values.
	 */
	@XmlValue
	String name;
	
	/**
	 * Convert a string input value to the output data type. The resulting type depends
	 * on the type attribute and can be one of Long, BigDecimal, String, DateTime
	 * or DateTimeAccuracy (for incomplete dates).
	 * <p>
	 * TODO: how to read SQL table data, which already contains types (e.g. sql.Integer)
	 * 
	 * @param value input value. e.g. from text table column
	 * @return output type representing the input value
	 */
	public Object valueOf(String value){
		if( constantValue != null ){
			value = constantValue;
		}
		
		if( na != null && value != null && na.equals(value) ){
			value = null;
		}
		
		if( value != null && regexMatch != null ){
			value = applyRegularExpression(value);
		}
		
		return value;
	}
	
	public String applyRegularExpression(String input){
		// TODO: apply
		return input;
	}
	
	public static class IntegerColumn extends Column{
		
	}
	public static class DecimalColumn extends Column{
		@XmlTransient
		DecimalFormat decimalFormat;
		
		/**
		 * Decimal format string for parsing via {@link DecimalFormat}
		 * @see DecimalFormat#DecimalFormat(String)
		 */
		@XmlAttribute
		String format;
	}
}
