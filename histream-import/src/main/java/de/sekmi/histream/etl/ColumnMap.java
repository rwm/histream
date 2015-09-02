package de.sekmi.histream.etl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.sekmi.histream.etl.config.Column;

/**
 * Maps {@link Column}s to header/table indices.
 * 
 * @author Raphael
 *
 */
public class ColumnMap{
	String[] headers;
	
	/**
	 * Maps column names to row indices
	 */
	Map<String, Integer> map;
	
	/**
	 * Construct a column map with ordered list of headers
	 * 
	 * @param headers headers
	 */
	public ColumnMap(String[] headers){
		this.headers = headers;
		this.map = new HashMap<>();
	}
	
	/**
	 * Register a column and lookup it's index in the header list.
	 * The index is stored for later retrieval via {@link #indexOf(Column)}.
	 * 
	 * @param column column to register
	 * @throws ParseException if the column cannot be found in the headers
	 * @throws NullPointerException if column is null
	 */
	public void registerColumn(Column<?> column)throws ParseException{
		Objects.requireNonNull(column);
		column.validate(); // TODO: maybe call after unmarshal of column
		
		if( column.getName().isEmpty() ){
			// no reference to column, probably constant value
			// no need to register
			return;
		}
		
		if( map.containsKey(column.getName()) ){
			// column name already registered
			return;
		}
		// find name and map to index
		for( int i=0; i<headers.length; i++ ){
			if( headers[i].equals(column.getName()) ){
				map.put(column.getName(), Integer.valueOf(i) );
				return;
			}
		}
		throw new ParseException("Column name '"+column.getName()+"' not found in header");
	}
	
	/**
	 * Get header index of the specified column.
	 * 
	 * @param column column
	 * @return index in header list (specified in constructor)
	 */
	public Integer indexOf(Column<?> column){
		return map.get(column.getName());
	}
	
	/**
	 * Determine whether the specified header has a column associated
	 * 
	 * @param header header
	 * @return true if a column was registered, false otherwise
	 */
	public boolean isRegistered(String header){
		return map.containsKey(header);
	}
	/**
	 * Get the number of registered columns
	 * @return column count
	 */
	public int size(){
		return map.size();
	}
}