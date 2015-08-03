package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.VisitRow;

public class VisitTable extends Table<VisitRow> implements WideInterface{
	@XmlElement
	IDAT idat;
	
	@XmlElementWrapper(name="mdat")
	@XmlElement(name="concept")
	Concept[] concepts;
	
	@XmlType(name="patient-idat")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class IDAT extends IdatColumns{
		@XmlElement(name="visit-id")
		StringColumn visitId;
		DateTimeColumn start;
		DateTimeColumn end;
		// TODO inpatient/outpatient state
		Column[] ignore;
	}
	@Override
	public ColumnMap getColumnMap(String[] headers) {
		ColumnMap map = new ColumnMap(headers);
		map.registerColumn(idat.patientId);
		map.registerColumn(idat.visitId);
		map.registerColumn(idat.start);
		map.registerColumn(idat.end);
		for( Concept c : concepts ){
			mapRegisterConcept(map, c);
		}
		return map;
	}
	@Override
	public VisitRow fillRecord(ColumnMap map, Object[] row) throws ParseException {
		// TODO Auto-generated method stub
		return null;
	}
}
