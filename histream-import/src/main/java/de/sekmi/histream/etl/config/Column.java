package de.sekmi.histream.etl.config;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ParseException;

/**
 * Abstract table column.
 * A column does not need a name, if it has a constant-value assigned.
 * 
 * @author marap1
 * @param <T> column type
 *
 */
@XmlTransient
@XmlSeeAlso({StringColumn.class,IntegerColumn.class,DateTimeColumn.class,DecimalColumn.class})
public abstract class Column<T> {
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
	 * Column name to use for reading input values
	 * @return column name
	 */
	public String getName(){return name;}
	
	public abstract T valueOf(Object input) throws ParseException;
	
	/**
	 * Convert a string input value to the output data type. The resulting type depends
	 * on the type attribute and can be one of Long, BigDecimal, String, DateTime
	 * or DateTimeAccuracy (for incomplete dates).
	 * <p>
	 * TODO: how to read SQL table data, which already contains types (e.g. sql.Integer)
	 * 
	 * @param value input value. e.g. from text table column
	 * @return output type representing the input value
	 * @throws ParseException on errors with regular expressions
	 */
	public Object preprocessValue(Object value)throws ParseException{
		if( constantValue != null ){
			value = constantValue;
		}
		
		if( na != null && value != null && na.equals(value) ){
			value = null;
		}
		
		if( value != null && regexMatch != null ){
			if( !(value instanceof String) ){
				throw new ParseException("regex-match can only be used on String, but found "+value.getClass().getName());
			}
			
			value = applyRegularExpression((String)value);
		}
		
		return value;
	}
	
	public T valueOf(ColumnMap map, Object[] row) throws ParseException{
		if( name.isEmpty() ){
			// use constant value if available
			return valueOf(null);
		}
		Objects.requireNonNull(map);
		Objects.requireNonNull(row);
		Integer index = map.indexOf(this);
		Objects.requireNonNull(index);
		return this.valueOf(row[index]);
	}

	public String applyRegularExpression(String input){
		// TODO: apply
		return input;
	}
	
	public void validate()throws ParseException{
		if( name.isEmpty() && constantValue == null ){
			throw new ParseException("Empty column name only allowed if constant-value is specified");
		}
	}
}
