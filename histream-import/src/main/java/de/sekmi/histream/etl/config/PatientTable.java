package de.sekmi.histream.etl.config;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ConceptTable;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient.Sex;

/**
 * Patient table. Contains patient id and other identifying information.
 * Can also contain medical data
 * @author marap1
 *
 */
public class PatientTable extends Table<PatientRow> implements ConceptTable{
	@XmlElement
	IDAT idat;
	
	/**
	 * MDAT concepts
	 */
	@XmlElementWrapper(name="mdat")
	@XmlElement(name="concept")
	Concept[] concepts;
	/**
	 * Columns which will be ignored during processing
	 */
	@XmlElement
	Column<?>[] ignore;

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class IDAT extends IdatColumns{
		@XmlElement(name="given-name")
		StringColumn givenName;
		StringColumn surname;
		DateTimeColumn birthdate;
		DateTimeColumn deathdate; // TODO deathdate is not IDAT
		StringColumn gender;
	}
	// TODO attribute to reduce accuracy of birthdate/deathdate for privacy reasons. e.g. output-resolution=Years.

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
		Table.validateAllHeaders(headers, map, this.ignore);
		
		return map;
	}
	

	@Override
	public PatientRow fillRecord(ColumnMap map, Object[] row, ExternalSourceType source, String location) throws ParseException {
		String patid = idat.patientId.valueOf(map, row, null);
		PatientRow patient = new PatientRow(patid);

		if( idat.givenName != null ) {
			patient.givenName = idat.givenName.valueOf(map, row, null);
		}

		if( idat.surname != null ) {
			patient.surname = idat.surname.valueOf(map, row, null);			
		}
		
		if( idat.birthdate != null ) {
			patient.birthdate = idat.birthdate.valueOf(map, row, null);
		}

		if( idat.deathdate != null ) {
			patient.deathdate = idat.deathdate.valueOf(map, row, null);
		}

		if( idat.gender != null ){
			String genderCode = idat.gender.valueOf(map, row);
			// gender may omitted
			if( genderCode != null ){
				try{
					patient.sex = Sex.valueOf(genderCode);
				}catch( IllegalArgumentException e ){
					throw new ParseException("Unsupported gender value '"+genderCode+"'. Use one of "+Arrays.toString(Sex.values()), location);
				}
			}
		}

		// concepts
		if( concepts != null ){
			// concepts without visit not supported
			throw new ParseException("Patient table should not provide concepts outside of visit", location);
//			for( Concept c : concepts ){
//				Observation o = c.createObservation(patient.getPatientId(), null, factory, map, row);
//				patient.getFacts().add(o);
//			}
		}
		return patient;
	}


	@Override
	public Concept[] getConcepts() {
		return concepts;
	}


	@Override
	protected void mapRegisterConcept(ColumnMap map, Concept c) throws ParseException {
		if( true ){
			throw new ParseException("Patient table concepts not supported yet");
		}
		if( c.start == null ){
			// patient table does not have a default timestamp, facts always need explici timestamps
			throw new ParseException("Start timestamp required for patient table concept '"+c.id+"'");
		}
		super.mapRegisterConcept(map, c);
	}
	
}
