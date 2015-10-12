package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ConceptTable;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.PatientRow;

/**
 * Patient table. Contains patient id and other identifying information.
 * Can also contain medical data
 * @author marap1
 *
 */
public class PatientTable extends Table<PatientRow> implements ConceptTable{
	@XmlElement
	IDAT idat;
	
	@XmlElementWrapper(name="mdat")
	@XmlElement(name="concept")
	Concept[] concepts;

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class IDAT extends IdatColumns{
		@XmlElement(name="given-name")
		StringColumn givenName;
		StringColumn surname;
		DateTimeColumn birthdate;
		DateTimeColumn deathdate;
		StringColumn gender;
		Column<?>[] ignore;
	}

	@Override
	public ColumnMap getColumnMap(String[] headers) throws ParseException {
		ColumnMap map = new ColumnMap(headers);
		
		// need patientId at minimum
		if( idat.patientId == null ){
			throw new ParseException("datasource/patient-table/idat/patient-id column not specified");
		}
		map.registerColumn(idat.patientId);
		
		// other columns are optional
		if( idat.givenName != null ){
			map.registerColumn(idat.givenName);
		}
		if( idat.surname != null ){
			map.registerColumn(idat.surname);
		}
		if( idat.birthdate != null ){
			map.registerColumn(idat.birthdate);
		}
		if( idat.deathdate != null ){
			map.registerColumn(idat.deathdate);
		}
		if( idat.gender != null ){
			map.registerColumn(idat.gender);
		}
		// make sure all headers are specified in configuration
		Table.validateAllHeaders(headers, map, idat.ignore);
		
		return map;
	}
	

	@Override
	public PatientRow fillRecord(ColumnMap map, Object[] row, ObservationFactory factory) throws ParseException {
		PatientRow patient = new PatientRow();
		patient.setId(idat.patientId.valueOf(map, row));
		patient.setGivenName(idat.givenName.valueOf(map, row));
		patient.setSurname(idat.surname.valueOf(map, row));
		patient.setBirthDate(idat.birthdate.valueOf(map, row));
		if( idat.deathdate != null ){
			patient.setDeathDate(idat.deathdate.valueOf(map, row));
		}
		// concepts
		if( concepts != null ){
			for( Concept c : concepts ){
				Observation o = c.createObservation(patient.getPatientId(), null, factory, map, row);
				patient.getFacts().add(o);
			}
		}
		return patient;
	}


	@Override
	public Concept[] getConcepts() {
		return concepts;
	}
	
}
