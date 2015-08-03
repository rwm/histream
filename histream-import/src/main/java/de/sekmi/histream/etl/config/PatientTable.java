package de.sekmi.histream.etl.config;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.PatientRow;

/**
 * Patient table. Contains patient id and other identifying information.
 * Can also contain medical data
 * @author marap1
 *
 */
public class PatientTable extends Table<PatientRow> implements WideInterface{
	@XmlElement
	IDAT idat;
	
	@XmlElementWrapper(name="mdat")
	@XmlElement(name="concept")
	Concept[] concepts;

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class IDAT extends IdatColumns{
		StringColumn firstname;
		StringColumn surname;
		DateTimeColumn birthdate;
		DateTimeColumn deathdate;
		StringColumn gender;
		Column[] ignore;
	}

	@Override
	public ColumnMap getColumnMap(String[] headers) {
		ColumnMap map = new ColumnMap(headers);
		if( !map.registerColumn(idat.patientId) ){
			throw new IllegalArgumentException("patientId column name '"+idat.patientId.name+"' not found in patient table headers");
		}
		if( idat.firstname != null && !map.registerColumn(idat.firstname) ){
			throw new IllegalArgumentException("firstname column not found in patient header");
		}
		if( idat.surname != null && !map.registerColumn(idat.surname) ){
			throw new IllegalArgumentException("surname column not found in patient header");
		}
		if( idat.birthdate != null && !map.registerColumn(idat.birthdate) ){
			throw new IllegalArgumentException("birthdate column not found in patient header");
		}
		if( idat.deathdate != null && !map.registerColumn(idat.deathdate) ){
			throw new IllegalArgumentException("deathdate column not found in patient header");
		}
		if( idat.gender != null && !map.registerColumn(idat.gender) ){
			throw new IllegalArgumentException("gender column not found in patient header");
		}
		return map;
	}
	

	@Override
	public PatientRow fillRecord(ColumnMap map, Object[] row) throws ParseException {
		PatientRow patient = new PatientRow();
		patient.setId(idat.patientId.valueOf(map, row).toString());
		patient.setNames((String)idat.firstname.valueOf(map, row), (String)idat.surname.valueOf(map, row));
		patient.setBirthDate((DateTimeAccuracy)idat.birthdate.valueOf(map, row));
		patient.setDeathDate((DateTimeAccuracy)idat.deathdate.valueOf(map, row));
		// TODO concepts
		return patient;
	}
	
}
