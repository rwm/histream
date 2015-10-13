package de.sekmi.histream.etl.config;

import java.io.IOException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.FactRow;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.RecordSupplier;

@XmlTransient
public abstract class Table<T extends FactRow> {

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
	
	/**
	 * Make sure all columns are either registered in the column map or specified in an 'ignore' element.
	 * Otherwise, a {@link ParseException} is thrown.
	 * 
	 * @param headers table headers
	 * @param map column map
	 * @param ignored ignored columns
	 * @throws ParseException thrown for the first header which is neither in the map nor in ignored columns
	 */
	protected static void validateAllHeaders(String[] headers, ColumnMap map, Column<?>[] ignored) throws ParseException{
		// ignored missing?
		if( ignored == null ){
			// create empty array
			ignored = new Column<?>[]{};
		}else if( ignored.length == 1 && ignored[0].getName().equals("*") ){
			// ignore all other columns, pass validation
			return;
		}

		// for each header
		for( int i=0; i<headers.length; i++ ){
			// check if in map
			if( map.isRegistered(headers[i]) )continue;
			
			// not registered in map
			// check if listed in ignore element
			int j=0;
			for( j=0; j<ignored.length; j++ ){
				if( headers[i].equals(ignored[j].getName()) )break;
			}
			if( j == ignored.length ){
				// unassigned column
				throw new ParseException("Unconfigured header: "+headers[i]);
			}
		}
	}
	
	/**
	 * Creates and fills a record from a table row.
	 * <p>
	 * The method can decide to ignore a row (and issue a warning)
	 * e.g if certain criteria are not met or the configuration
	 * requires some rows to be ignored. In this case, {@code null} is
	 * returned.
	 * 
	 * @param map column map
	 * @param row row data
	 * @param factory observation factory
	 * @return complete record or {@code null} if the row should be ignored
	 * @throws ParseException for parse errors
	 */
	public abstract T fillRecord(ColumnMap map, Object[] row, ObservationFactory factory) throws ParseException;
	
	public RecordSupplier<T> open(ObservationFactory factory, Meta meta) throws IOException, ParseException{
		return new RecordSupplier<>(source.rows(meta), this, factory, meta);
	}


}
