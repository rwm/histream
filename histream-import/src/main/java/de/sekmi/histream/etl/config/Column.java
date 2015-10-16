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
	
	/**
	 * Create the column type value from a string representation.
	 * This will be used for the {@code constant-value} attribute is present
	 * and also to process data from string only sources like CSV or text tables.
	 * <p>
	 * The resulting type depends on the type attribute and can be one 
	 * of Long, BigDecimal, String, DateTime or DateTimeAccuracy (for incomplete dates).
	 * 
	 * @param input string value e.g. from text table column. This parameter is guaranteed to be non null.
	 * @return column output type representing the input value
	 * @throws ParseException if the string could not be parsed to the resulting data type
	 */
	public abstract T valueFromString(String input)throws ParseException;
	
	/**
	 * Convert another data type to the column data type.
	 * 
	 * @param input input value e.g. from SQL result set. 
	 * This parameter is guaranteed to be non null, since null values
	 * are handled before this method is called. 
	 * <p>
	 * This parameter is also guaranteed not to be of String type,
	 * since strings are handled via {@link #valueFromString(String)}
	 * 
	 * @return column data type
	 * @throws ParseException if conversion of data types failed
	 */
	public abstract T valueOf(Object input) throws ParseException;
	
	private String applyRegexReplace(String value){
		// TODO apply replace
		return value;
	}

	public T valueOf(ColumnMap colMap, Object[] row) throws ParseException{
		T ret;
		// use constant value if available
		if( constantValue != null ){
			// check for NA
			if( na != null && na.equals(constantValue) ){
				ret = null; // will result in null value
			}else{
				ret = valueFromString(constantValue); // use constant value
			}
		}else if( column == null || column.isEmpty() ){
			// no constant value and column undefined
			// the column will always produce null values
			ret = null;
		}else{
			// use actual row value
			Objects.requireNonNull(colMap);
			Objects.requireNonNull(row);
			Integer index = colMap.indexOf(this);
			Objects.requireNonNull(index);
			Object rowval = row[index];
			// string processing (na, regex-replace, mapping) only performed on string values
			if( rowval == null ){
				ret = null; // null value
			}else if( rowval.getClass() == String.class ){
				// non null string value
				String val = (String)rowval;
				// apply regular expression replacements
				if( regexReplace != null ){
					val = applyRegexReplace(val);
				}
				// TODO apply map rules
				// check for NA
				if( na != null && val != null && na.equals(val) ){
					val = null;
				}
				// convert value
				if( val != null ){
					ret = valueFromString(val);
				}else{
					ret = null;
				}
			}else if( na != null || regexReplace != null || map != null ){
				throw new ParseException("String operation (na/regexReplace/map) defined for column "+getName()+", but table provides type "+rowval.getClass().getName()+" instead of String");
			}else{
				// other non string value without string processing
				ret = valueOf(rowval); // use value directly
			}
		}
		return ret;
	}
	
	public void validate()throws ParseException{
		if( column == null && constantValue == null ){
			throw new ParseException("Empty column name only allowed if constant-value is specified");
		}else if( column != null && column.isEmpty() ){
			throw new ParseException("No empty column attribute allowed. Remove attribute for constant values");
		}
	}
}
