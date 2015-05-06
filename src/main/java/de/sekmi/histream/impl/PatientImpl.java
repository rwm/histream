package de.sekmi.histream.impl;


import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.StoredExtensionType;

/**
 * Implementation of the Patient interface.
 * @author Raphael
 *
 */
public class PatientImpl extends StoredExtensionType implements Patient {
	private DateTimeAccuracy birthDate;
	private DateTimeAccuracy deathDate;
	private Sex sex;
	
	public PatientImpl(){
		
	}
	
	public PatientImpl(String id, Sex sex, DateTimeAccuracy birthDate, DateTimeAccuracy deathDate){
		setId(id);
		this.sex = sex;
		this.birthDate = birthDate;
		this.deathDate = deathDate;
	}
	
	@Override
	public DateTimeAccuracy getBirthDate() {
		return birthDate;
	}
	
	@Override
	public void setBirthDate(DateTimeAccuracy dateTime){
		// TODO compare and markDirty  if different (also in other setters)
		this.birthDate = dateTime;
	}

	@Override
	public DateTimeAccuracy getDeathDate() {
		return deathDate;
	}
	
	@Override
	public void setDeathDate(DateTimeAccuracy dateTime){
		this.deathDate = dateTime;
	}

	@Override
	public Sex getSex() {
		return sex;
	}

	@Override
	public void setSex(Sex sex) {
		this.sex = sex;
	}


}
