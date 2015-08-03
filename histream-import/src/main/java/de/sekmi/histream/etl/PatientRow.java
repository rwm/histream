package de.sekmi.histream.etl;

import java.time.Instant;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.etl.config.PatientTable;
import de.sekmi.histream.ext.Patient;

/**
 * Row of patient data
 * TODO: implement {@link Patient}
 * @author Raphael
 *
 */
public class PatientRow implements Patient{
	String patid;
	String firstname;
	String lastname;
	DateTimeAccuracy birthdate;
	DateTimeAccuracy deathdate;
	// TODO concepts
	

	public void setNames(String first, String last){
		this.firstname = first;
		this.lastname = last;
	}
	public void setBirthDate(DateTimeAccuracy date){
		this.birthdate = date;
	}
	public void setDeathDate(DateTimeAccuracy date){
		this.deathdate = date;
	}
	
	
	public static PatientRow load(Object[] input, ColumnMap map, PatientTable table){
		PatientRow row = new PatientRow();
		row.patid = null; // table.patid.valueOf(input[map.findIndex(table.patid)])
		return row;
	}
	@Override
	public String getId() {
		return patid;
	}
	@Override
	public void setId(String patientId) {
		this.patid = patientId;
	}
	@Override
	public String getSourceId() {
		return null;
	}
	@Override
	public Instant getSourceTimestamp() {
		return null;
	}
	@Override
	public void setSourceId(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setSourceTimestamp(Instant arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public DateTimeAccuracy getBirthDate() {
		return this.birthdate;
	}
	@Override
	public DateTimeAccuracy getDeathDate() {
		return this.deathdate;
	}
	@Override
	public Sex getSex() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setSex(Sex arg0) {
		// TODO Auto-generated method stub
		
	}
}
