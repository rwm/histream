package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ObservationFactory;
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
	public ColumnMap getColumnMap(String[] headers) throws ParseException {
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
	public VisitRow fillRecord(ColumnMap map, Object[] row, ObservationFactory factory) throws ParseException {
		VisitRow visit = new VisitRow();
		visit.setId(idat.visitId.valueOf(map, row).toString());
		visit.setPatientId(idat.patientId.valueOf(map, row).toString());
		visit.setStartTime((DateTimeAccuracy)idat.start.valueOf(map, row));
		visit.setEndTime((DateTimeAccuracy)idat.end.valueOf(map, row));
		// TODO other 
		// TODO concepts
		return visit;
	}}
