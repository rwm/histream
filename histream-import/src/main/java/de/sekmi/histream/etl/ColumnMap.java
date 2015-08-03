package de.sekmi.histream.etl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.sekmi.histream.etl.config.Column;

/**
 * Maps {@link Column}s to header/table indices
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
	
	public ColumnMap(String[] headers){
		this.headers = headers;
		this.map = new HashMap<>();
	}
	
	/**
	 * Register a column and lookup it's index in the header list.
	 * @param column
	 * @throws ParseException if the column cannot be found in the headers
	 * @throws NullPointerException if column is null
	 */
	public void registerColumn(Column column)throws ParseException{
		Objects.requireNonNull(column);
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
	public Integer indexOf(Column column){
		return map.get(column.getName());
	}
}