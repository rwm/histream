package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.etl.ColumnMap;

@XmlTransient
public abstract class Table {

	@XmlElement(required=true)
	TableSource source;
	
	/**
	 * Retrieve a column map for this table
	 * @param headers row headers
	 * @return column map
	 */
	public abstract ColumnMap getColumnMap(String[] headers);
	
	/**
	 * Register all columns for the given concept to the column map
	 * @param map column map
	 * @param c concept to register
	 */
	protected void mapRegisterConcept(ColumnMap map, Concept c){
		map.registerColumn(c.start);
		map.registerColumn(c.end);
		map.registerColumn(c.value);
		map.registerColumn(c.unit);
		for( Concept.Modifier m : c.modifiers ){
			map.registerColumn(m.value);
			map.registerColumn(m.unit);
		}
	}
}
