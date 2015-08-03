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
	public ColumnMap getColumnMap(String[] headers) throws ParseException {
		ColumnMap map = new ColumnMap(headers);
		
		map.registerColumn(idat.patientId);
		map.registerColumn(idat.firstname);
		map.registerColumn(idat.surname);
		map.registerColumn(idat.birthdate);
		map.registerColumn(idat.deathdate);
		map.registerColumn(idat.gender);
		
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
