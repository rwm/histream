package de.sekmi.histream.etl.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps {@link Column}s to header/table indices
 * 
 * @author Raphael
 *
 */
public class ColumnMap{
	String[] headers;
	Map<String, Integer> map;
	
	public ColumnMap(String[] headers){
		this.headers = headers;
		this.map = new HashMap<>();
	}
	
	public boolean registerColumn(Column column){
		for( int i=0; i<headers.length; i++ ){
			if( column.name.equals(headers[i]) ){
				map.put(column.name, Integer.valueOf(i) );
				return true;
			}
		}
		return false;
	}
}