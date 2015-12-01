package de.sekmi.histream.etl.config;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.MapFeedback;
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
	 * Replace the input value with the specified string or regular expression group.
	 * If not specified, the full input string is used (regardless of match region).
	 * TODO remove?
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
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private void applyMapRules(String value, MapFeedback action){
		boolean match = false;
		// always perform operations specified in the map element
		// regardless of matching value
		if( map.setConcept != null ){
			action.overrideConcept(map.setConcept);
		}
		if( map.setValue != null ){
			action.overrideValue(map.setValue);
		}

		// XXX TODO implement setUnit 

		// iterate through listed cases
		// no case may be present at all
		if( map.cases != null ){
			for( MapCase mc : map.cases ){
				Objects.requireNonNull(mc.value);
				if( mc.value.equals(value) ){
					match = true;
					if( mc.setValue != null ){
						action.overrideValue(mc.setValue);
					}
					// set concept
					if( mc.setConcept != null ){
						action.overrideConcept(mc.setConcept);
					}
					// check action
					if( mc.action != null && mc.action.equals("drop-fact") ){
						action.dropFact();
						// TODO check after loading for illegal values or use enum
					}
					break;
				}
			}
		}
		if( match == false && map.otherwise != null ){
			if( map.otherwise.setValue != null ){
				action.overrideValue(map.otherwise.setValue);
			}
			if( map.otherwise.setConcept != null ){
				action.overrideConcept(map.otherwise.setConcept);
			}
			// check action
			if( map.otherwise.action != null && map.otherwise.action.equals("drop-fact") ){
				action.dropFact();
				// TODO check after loading for illegal values or use enum
			}
		}
	}

	/**
	 * Process and return the column value from a table row without map rule processing.
	 * This method behaves as if {@link #valueOf(ColumnMap, Object[], MapFeedback)} was called
	 * with the last argument set to {@code null}.
	 * 
	 * @see #valueOf(Object)
	 * @param colMap column map
	 * @param row table row
	 * @return value
	 * @throws ParseException parse errors
	 */
	public T valueOf(ColumnMap colMap, Object[] row) throws ParseException{
		return valueOf(colMap, row, null);
	}
	
	private T processedValue(String val, MapFeedback mapFeedback) throws ParseException{
		T ret;
		// apply regular expression replacements
		if( regexReplace != null ){
			val = applyRegexReplace(val);
		}
		// apply map rules
		if( map != null ){
			if( mapFeedback == null ){
				throw new ParseException("map element allowed for column "+getName());
			}
			applyMapRules(val, mapFeedback);
			// use value override, if present
			if( mapFeedback.getValueOverride() != null ){
				val = mapFeedback.getValueOverride();
			}
		}
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
		return ret;
	}

	/**
	 * Process and return the column value from a table row.
	 * 
	 * @param colMap column map
	 * @param row table row
	 * @param mapFeedback map rule feedback, can be set to {@code null} if map rules forbidden for this column.
	 * @return final column value
	 * @throws ParseException parse errors
	 */
	public T valueOf(ColumnMap colMap, Object[] row, MapFeedback mapFeedback) throws ParseException{
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
			// this should not happen -> concept neither constant value nor column name
		}else{
			// use actual row value
			// TODO merge with valueOf(Object,MapFeedback), but colmap lookup shall not occur for constant values
			Objects.requireNonNull(colMap);
			Objects.requireNonNull(row);
			Integer index = colMap.indexOf(this);
			Objects.requireNonNull(index);
			Object rowval = row[index];
			// string processing (na, regex-replace, mapping) only performed on string values
			if( rowval == null || rowval instanceof String ){
				// non null string value
				ret = processedValue((String)rowval, mapFeedback);
			}else if( na != null || regexReplace != null || map != null ){
				throw new ParseException("String operation (na/regexReplace/map) defined for column "+getName()+", but table source provides type "+rowval.getClass().getName()+" instead of String");
			}else{
				// other non string value without string processing
				ret = valueOf(rowval); // use value directly
			}
		}
		return ret;
	}
	
	/**
	 * Process and return the column value from a table row.
	 * Same as {@link #valueOf(ColumnMap, Object[], MapFeedback)} but without
	 * lookup with column map.
	 * TODO merge both methods
	 * 
	 * @param rowval value from row
	 * @param mapFeedback mapping feedback
	 * @return processed value
	 * @throws ParseException parse error
	 */
	public T valueOf(Object rowval, MapFeedback mapFeedback) throws ParseException{
		T ret;
		// use constant value if available
		if( constantValue != null ){
			// check for NA
			if( na != null && na.equals(constantValue) ){
				ret = null; // will result in null value
			}else{
				ret = valueFromString(constantValue); // use constant value
			}
		}if( rowval == null || rowval instanceof String ){
			ret = processedValue((String)rowval, mapFeedback);
		}else if( na != null || regexReplace != null || map != null ){
			throw new ParseException("String operation (na/regexReplace/map) defined for column "+getName()+", but table source provides type "+rowval.getClass().getName()+" instead of String");
		}else{
			// other non string value without string processing
			ret = valueOf(rowval); // use value directly
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
	@Override
	public String toString() {
		return "Column["+column+"]";
	}
}
