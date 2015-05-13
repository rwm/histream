package de.sekmi.histream.i2b2;

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
import de.sekmi.histream.impl.PatientImpl;

/**
 * I2b2 patient. The active patient_ide is returned by {@link #getId()}.
 * @author Raphael
 *
 */
public class I2b2Patient extends PatientImpl {
	/**
	 * i2b2 internal patient number (32bit integer)
	 */
	private int patient_num;
	
	/**
	 * merged patient_ide strings.
	 */
	String[] mergedIds;
	
	
	public I2b2Patient(int num){
		this.patient_num = num;
	}
	public I2b2Patient(int num, Sex sex, DateTimeAccuracy birthDate,
			DateTimeAccuracy deathDate) {
		this(num);
		setSex(sex);
		setBirthDate(birthDate);
		setDeathDate(deathDate);
	}

	public int getNum(){return patient_num;}
	public void setNum(int patient_num){this.patient_num = patient_num;}
	
}
