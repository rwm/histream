package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ConceptTable;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.PreparedObservation;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.VisitPatientImpl;

public class WideTable extends Table<WideRow> implements ConceptTable{

	@XmlElement
	DataTableIdat idat;
	
	@XmlElementWrapper(name="mdat")
	@XmlElement(name="concept")
	Concept[] concepts;

	@Override
	public ColumnMap getColumnMap(String[] headers) throws ParseException {
		ColumnMap map = new ColumnMap(headers);

		if( idat.patientId == null ){
			// missing patientId column is allowed. In these cases visitId should be unique for identification
			// TODO write INFO message that patient-id is not available and visit-id is used for matching
		}else {
			map.registerColumn(idat.patientId);
		}
		if( idat.visitId == null ){
			throw new ParseException("datasource/wide-table/idat/visit-id column not specified");
		}

		map.registerColumn(idat.visitId);
		
		for( Concept c : concepts ){
			mapRegisterConcept(map, c);
		}

		// TODO allow option/parameter to force column checking for data tables
		//validateAllHeaders(headers, map, idat.ignore);

		return map;
	}

	@Override
	public WideRow fillRecord(ColumnMap map, Object[] row, ExternalSourceType source, String recordOrigin) throws ParseException {
		String visit = idat.visitId.valueOf(map, row);
		String patid;
		if( idat.patientId == null ) {
			// if there is no patient id in this table, 
			// we can get the patient id by lookup by visit
			patid = null;
		}else {
			patid = idat.patientId.valueOf(map, row);
		}
		WideRow rec = new WideRow(this,patid,visit);
		rec.source = source;
		rec.recordOrigin = recordOrigin;

		for( Concept c : concepts ){
			PreparedObservation o = c.prepareObservation(map, row);
			// null is returned for action=drop-fact
			if( o != null ){
				rec.addPreparedObservation(o);
			}
		}
		return rec;
	}

	@Override
	public Concept[] getConcepts() {
		return concepts;
	}
}
