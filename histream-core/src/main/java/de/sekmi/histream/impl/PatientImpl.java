package de.sekmi.histream.impl;


/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.StoredExtensionType;

/**
 * Implementation of the Patient interface.
 * All setter methods will call {@code markDirty(true)} if the object was previously
 * not dirty and the new value is different from the old one.
 * 
 * @author R.W.Majeed
 *
 */
public class PatientImpl extends StoredExtensionType implements Patient {
	private DateTimeAccuracy birthDate;
	private DateTimeAccuracy deathDate;
	private Sex sex;
	private String surname, givenName;
	private Boolean deceased;
	
	public PatientImpl(){
		
	}
	
	public PatientImpl(String id, Sex sex, DateTimeAccuracy birthDate, DateTimeAccuracy deathDate){
		setId(id);
		this.sex = sex;
		this.birthDate = birthDate;
		setDeathDate(deathDate);
	}
	
	@Override
	public DateTimeAccuracy getBirthDate() {
		return birthDate;
	}
	
	@Override
	public void setBirthDate(DateTimeAccuracy dateTime){
		checkAndUpdateDirty(this.birthDate, dateTime);
		this.birthDate = dateTime;
	}

	@Override
	public DateTimeAccuracy getDeathDate() {
		return deathDate;
	}
	
	@Override
	public void setDeathDate(DateTimeAccuracy dateTime){
		checkAndUpdateDirty(this.deathDate, dateTime);
		this.deathDate = dateTime;
		// non null death date implies patient deceased
		if( dateTime != null ){
			this.deceased = true;
		}
	}

	@Override
	public Sex getSex() {
		return sex;
	}

	@Override
	public void setSex(Sex sex) {
		checkAndUpdateDirty(this.sex, sex);
		this.sex = sex;
	}

	@Override
	public String getSurname() {
		return surname;
	}

	@Override
	public void setSurname(String surname) {
		checkAndUpdateDirty(this.surname, surname);
		this.surname = surname;
	}

	@Override
	public String getGivenName() {
		return givenName;
	}

	@Override
	public void setGivenName(String givenName) {
		checkAndUpdateDirty(this.givenName, givenName);
		this.givenName = givenName;
	}

	@Override
	public Boolean getDeceased() {
		return deceased;
	}

	@Override
	public void setDeceased(Boolean deceased) {
		checkAndUpdateDirty(this.deceased, deceased);
		this.deceased = deceased;
	}


	@Override
	public String toString(){
		return "Patient(id="+getId()+", dob="+getBirthDate()+")";
	}

}
