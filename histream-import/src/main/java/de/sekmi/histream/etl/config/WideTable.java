package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import de.sekmi.histream.etl.ColumnMap;

public class WideTable extends Table {

	@XmlElement
	DataTableIdat idat;
	
	@XmlElementWrapper(name="mdat")
	@XmlElement(name="concept")
	Concept[] concepts;

	@Override
	public ColumnMap getColumnMap(String[] headers) {
		ColumnMap map = new ColumnMap(headers);
		map.registerColumn(idat.patientId);
		map.registerColumn(idat.visitId);
		for( Concept c : concepts ){
			mapRegisterConcept(map, c);
		}
		return map;
	}
}
