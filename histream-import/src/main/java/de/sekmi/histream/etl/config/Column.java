package de.sekmi.histream.etl.config;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

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
		this.column = name;
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
	 * Replace the input value with the specified string or regular expression group from {@link #regexMatch}.
	 * If not specified, the full input string is used (regardless of match region).
	 */
	@XmlAttribute(name="regex-replace")
	String regexReplace;
	
	/**
	 * Column name to use for reading input values.
	 */
	@XmlAttribute(required=true)
	String column;
	
	
	@XmlElement(required=false)
	MapRules map;
	
	/**
	 * Column name to use for reading input values
	 * @return column name
	 */
	public String getName(){return column;}
	
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
		// use constant value if provided
		if( constantValue != null ){
			value = constantValue;
		}
		
		// apply regular expression replacements
		if( value != null && regexReplace != null ){
			
			value = applyRegexReplace((String)value);
		}
		
		// apply map rules
		if( map != null ){
			// TODO apply map rules
			// TODO find way to communicate warnings
			// TODO find way to set action (inplace/drop/generate)
		}
		
		// check for na result
		if( na != null && value != null && na.equals(value) ){
			value = null;
		}
		

		return value;
	}
	
	private String applyRegexReplace(String value){
		// TODO apply replace
		return value;
	}
	public T valueOf(ColumnMap map, Object[] row) throws ParseException{
		if( column == null || column.isEmpty() ){
			// use constant value if available
			return valueOf(null);
		}
		Objects.requireNonNull(map);
		Objects.requireNonNull(row);
		Integer index = map.indexOf(this);
		Objects.requireNonNull(index);
		return this.valueOf(row[index]);
	}
	
	public void validate()throws ParseException{
		if( column == null && constantValue == null ){
			throw new ParseException("Empty column name only allowed if constant-value is specified");
		}else if( column != null && column.isEmpty() ){
			throw new ParseException("No empty column attribute allowed. Remove attribute for constant values");
		}
	}
}
