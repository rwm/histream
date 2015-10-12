package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ConceptTable;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.VisitRow;

public class VisitTable extends Table<VisitRow> implements ConceptTable{
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
		StringColumn location;
		// TODO inpatient/outpatient state
		Column<?>[] ignore;
	}
	@Override
	public ColumnMap getColumnMap(String[] headers) throws ParseException {
		ColumnMap map = new ColumnMap(headers);

		if( idat.patientId == null ){
			throw new ParseException("datasource/visit-table/idat/patient-id column not specified");
		}
		if( idat.visitId == null ){
			throw new ParseException("datasource/visit-table/idat/visit-id column not specified");
		}

		map.registerColumn(idat.patientId);
		map.registerColumn(idat.visitId);
		if( idat.start != null ){
			map.registerColumn(idat.start);			
		}else{
			throw new ParseException("datasource/visit-table/idat/start column required");
		}
		if( idat.end != null ){
			map.registerColumn(idat.end);
		}
		if( idat.location != null ){
			map.registerColumn(idat.location);
		}
		for( Concept c : concepts ){
			mapRegisterConcept(map, c);
		}

		// make sure all columns are specified
		validateAllHeaders(headers, map, idat.ignore);

		return map;
	}

	@Override
	public VisitRow fillRecord(ColumnMap map, Object[] row, ObservationFactory factory) throws ParseException {
		VisitRow visit = new VisitRow();
		visit.setId(idat.visitId.valueOf(map, row));
		visit.setPatientId(idat.patientId.valueOf(map, row));
		DateTimeAccuracy start = idat.start.valueOf(map, row);
		if( start == null ){
			// no start time specified for visit row
			// any other way to retrieve a timestamp??
			throw new ParseException("No start timestamp found for visit row, but needed for observation");
		}
		visit.setStartTime(start);
		if( idat.end != null ){
			visit.setEndTime(idat.end.valueOf(map, row));
		}
		
		if( idat.location != null ){
			visit.setLocationId(idat.location.valueOf(map, row));
		}
		// TODO other 
		
		// concepts
		if( concepts != null ){
			for( Concept c : concepts ){
				Observation o = c.createObservation(visit.getPatientId(), visit.getId(), factory, map, row);
				visit.getFacts().add(o);
			}
		}
		return visit;
	}

	@Override
	public Concept[] getConcepts() {
		return concepts;
	}
}
