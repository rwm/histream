package de.sekmi.histream.i2b2;

import java.time.temporal.ChronoUnit;

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

	/**
	 * Get the i2b2 vital_status_cd for a patient.
	 * Values Y,M,X,R,T,S can be returned.
	 * @param patient patient object
	 * @return vital status code, see CRC_Design doc
	 */
	public String getVitalStatusCd(){
		Patient patient = this;
		char death_char=0, birth_char=0;

		if( patient.getDeathDate() != null ){
			switch( patient.getDeathDate().getAccuracy() ){
			case DAYS:
				death_char = 'Y';
				break;
			case MONTHS:
				death_char = 'M';
				break;
			case YEARS:
				death_char = 'X';
				break;
			case HOURS:
				death_char = 'R';
				break;
			case MINUTES:
				death_char = 'T';
				break;
			case SECONDS:
				death_char = 'S';
				break;
			default:
			}
		}else{
			// no death date available
			Boolean deceased = patient.getDeceased();
			if( deceased == null ){
				death_char = 'U'; // unknown
			}else if( deceased.booleanValue() == true ){
				death_char = 'Z'; // deceased
			}else if( deceased.booleanValue() == false ){
				// living
				death_char = 0; // null death char
				death_char = 'N'; // same meaning
			}
		}

		// birth date
		if( patient.getBirthDate() != null ){
			switch( patient.getBirthDate().getAccuracy() ){
			case DAYS:
				birth_char = 'D';
				break;
			case MONTHS:
				birth_char = 'B';
				break;
			case YEARS:
				birth_char = 'F';
				break;
			case HOURS:
				birth_char = 'H';
				break;
			case MINUTES:
				birth_char = 'I';
				break;
			case SECONDS:
				birth_char = 'C';
				break;
			default:
			}
		}else{
			// birth date unknown
			birth_char = 'L';
		}

		if( death_char != 0 && birth_char != 0 )
			return new String(new char[]{death_char,birth_char});
		else if( death_char != 0 )
			return new String(new char[]{death_char});
		else if( birth_char != 0 )
			return new String(new char[]{birth_char});
		else return null;
	}

	/**
	 * Set the vital status code, which also determines the accuracy
	 * of birth date and deceased date. This method should be called AFTER
	 * setting the birth and death date.
	 *
	 * @param vital_cd vital code
	 */
	public void setVitalStatusCd(String vital_cd){
		Patient patient = this;
		ChronoUnit accuracy = null;
		char birthIndicator = 0;
		char deathIndicator = 0;
		// load accuracy
		if( vital_cd == null || vital_cd.length() == 0 ){
			// living patient, birth date accurate to day
			birthIndicator = 0;
			deathIndicator = 0;
		}else{
			deathIndicator = vital_cd.charAt(0);
		}
		
		
		// death date indicator
		switch( deathIndicator ){
		case 'U': // unknown, no date
			setDeathDate(null);
			setDeceased(null);
			break;
		case 'Z': // deceased, no date
			setDeathDate(null);
			setDeceased(true);
			break;
		case 'Y': // deceased, accurate to day
			accuracy = ChronoUnit.DAYS;
			break;
		case 'M': // deceased, accurate to month
			accuracy = ChronoUnit.MONTHS;
			break;
		case 'X': // deceased, accurate to year
			accuracy = ChronoUnit.YEARS;
			break;
		case 'R': // deceased, accurate to hour
			accuracy = ChronoUnit.HOURS;
			break;
		case 'T': // deceased, accurate to minute
			accuracy = ChronoUnit.MINUTES;
			break;
		case 'S': // deceased, accurate to second
			accuracy = ChronoUnit.SECONDS;
			break;
		default:
			// no match for death status -> check for birth status in first character
			birthIndicator = deathIndicator;
		case 0:
		case 'N': // living, no date
			setDeathDate(null);
			setDeceased(false);
			break;
		}

		if( patient.getDeathDate() != null && accuracy != null ){
			patient.getDeathDate().setAccuracy(accuracy);
			patient.setDeceased(true);
		}

		accuracy = null;
		if( vital_cd != null && vital_cd.length() > 1 ){
			// use second character if available
			birthIndicator = vital_cd.charAt(1);
		}

		// birth date indicator
		switch( birthIndicator ){
		case 'L': // unknown, no date
			setBirthDate(null);
			break;
		case 0:
		case 'D': // known, accurate to day
			accuracy = ChronoUnit.DAYS;
			break;
		case 'B': // known, accurate to month
			accuracy = ChronoUnit.MONTHS;
			break;
		case 'F': // known, accurate to year
			accuracy = ChronoUnit.YEARS;
			break;
		case 'H': // known, accurate to hour
			accuracy = ChronoUnit.HOURS;
			break;
		case 'I': // known, accurate to minute
			accuracy = ChronoUnit.MINUTES;
			break;
		case 'C': // known, accurate to second
			accuracy = ChronoUnit.SECONDS;
			break;
		}
		if( patient.getBirthDate() != null && accuracy != null ){
			patient.getBirthDate().setAccuracy(accuracy);
		}		
	}


}
