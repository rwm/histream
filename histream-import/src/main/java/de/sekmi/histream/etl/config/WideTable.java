package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.WideRow;

public class WideTable extends Table<WideRow> {

	@XmlElement
	DataTableIdat idat;
	
	@XmlElementWrapper(name="mdat")
	@XmlElement(name="concept")
	Concept[] concepts;

	@Override
	public ColumnMap getColumnMap(String[] headers) throws ParseException {
		ColumnMap map = new ColumnMap(headers);
		map.registerColumn(idat.patientId);
		map.registerColumn(idat.visitId);
		for( Concept c : concepts ){
			mapRegisterConcept(map, c);
		}
		return map;
	}

	@Override
	public WideRow fillRecord(ColumnMap map, Object[] row, ObservationFactory factory) throws ParseException {
		String patid = idat.patientId.valueOf(map, row);
		String visit = idat.visitId.valueOf(map, row);
		WideRow rec = new WideRow(patid,visit);
		for( Concept c : concepts ){
			Observation o = c.createObservation(patid, visit, factory, map, row);
			rec.addFact(o);
		}
		return rec;
	}
}
