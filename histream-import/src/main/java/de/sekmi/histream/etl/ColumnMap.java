package de.sekmi.histream.etl;

import java.util.HashMap;
import java.util.Map;

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
	
	public boolean registerColumn(Column column){
		if( map.containsKey(column.getName()) ){
			// column name already registered
			return true;
		}
		// find name and map to index
		for( int i=0; i<headers.length; i++ ){
			if( headers[i].equals(column.getName()) ){
				map.put(column.getName(), Integer.valueOf(i) );
				return true;
			}
		}
		return false;
	}
	public Integer indexOf(Column column){
		return map.get(column.getName());
	}
}