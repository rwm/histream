package de.sekmi.histream.etl.config;

import java.io.IOException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.RecordSupplier;

@XmlTransient
public abstract class Table<T> {

	@XmlElement(required=true)
	TableSource source;
	
	/**
	 * Retrieve a column map for this table
	 * @param headers row headers
	 * @return column map
	 * @throws ParseException if the map cannot be constructed
	 */
	public abstract ColumnMap getColumnMap(String[] headers) throws ParseException;
	
	/**
	 * Register all columns for the given concept to the column map
	 * @param map column map
	 * @param c concept to register
	 * @throws ParseException if headers could not be found/mapped
	 */
	protected void mapRegisterConcept(ColumnMap map, Concept c) throws ParseException{
		map.registerColumn(c.start);
		if( c.end != null ){
			map.registerColumn(c.end);
		}
		if( c.value != null ){
			map.registerColumn(c.value);
		}
		if( c.unit != null ){
			map.registerColumn(c.unit);
		}
		if( c.modifiers != null ){
			for( Concept.Modifier m : c.modifiers ){
				map.registerColumn(m.value);
				if( m.unit != null ){
					map.registerColumn(m.unit);
				}
			}
		}
	}
	
	public abstract T fillRecord(ColumnMap map, Object[] row, ObservationFactory factory) throws ParseException;
	
	public RecordSupplier<T> open(ObservationFactory factory) throws IOException, ParseException{
		return new RecordSupplier<>(source.rows(), this, factory);
	}


}
