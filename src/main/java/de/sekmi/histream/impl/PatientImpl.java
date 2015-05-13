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
