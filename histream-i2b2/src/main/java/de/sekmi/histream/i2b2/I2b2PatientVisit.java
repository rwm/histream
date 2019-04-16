package de.sekmi.histream.i2b2;

import java.time.temporal.ChronoUnit;

import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.PatientImpl;

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

import de.sekmi.histream.impl.VisitPatientImpl;


/**
 * I2b2 visit. The active encounter_ide is returned by {@link #getId()}.
 * 
 * @author Raphael
 *
 */
public class I2b2PatientVisit extends VisitPatientImpl {

	/**
	 * I2b2 internal encounter id (32bit integer)
	 */
	private int encounter_num;
	private int patient_num;
	
	/**
	 * String id aliases for the encounter
	 */
	String[] aliasIds;
	/**
	 * Index in aliasIds for the primary alias
	 */
	int primaryAliasIndex;
	
	int maxInstanceNum;
	
	public I2b2PatientVisit(int encounter_num, int patient_num) {
		super();
		this.encounter_num = encounter_num;
		this.patient_num = patient_num;
		maxInstanceNum = 1;
		// TODO set startDate, endDate
		
	}

	public int getNum(){return encounter_num;}
	public int getPatientNum(){return patient_num;}

	@Override
	public void setPatient(PatientImpl patient) {
		super.setPatient(patient);
		if( patient instanceof I2b2Patient ) {
			// also set the patient_num 
			int patient_num = ((I2b2Patient)patient).getNum();
			this.patient_num = patient_num;
		}else {
			throw new IllegalArgumentException("Patient expected of instanceOf I2b2Patient");
		}
	}

	@Override
	public String toString(){
		return "I2b2Visit(encounter_um="+encounter_num+")";
	}

	/**
	 * Get the i2b2 vital_status_cd for this visit.
	 * @return vital status code, see CRC_Design doc
	 */
	public String getActiveStatusCd(){
		Visit visit = this;
		char end_char=0, start_char=0;
		if( visit.getEndTime() != null ){
			switch( visit.getEndTime().getAccuracy() ){
			case DAYS:
				end_char = 0; // same meaning
				end_char = 'Y';
				break;
			case MONTHS:
				end_char = 'M';
				break;
			case YEARS:
				end_char = 'X';
				break;
			case HOURS:
				end_char = 'R';
				break;
			case MINUTES:
				end_char = 'T';
				break;
			case SECONDS:
				end_char = 'S';
				break;
			default:
			}
		}else{
			// null end date
			// U: unknown, O: ongoing
			// default to unknown
			end_char = 'U';
		}

		// start date
		if( visit.getStartTime() != null ){
			switch( visit.getStartTime().getAccuracy() ){
			case DAYS:
				start_char = 0; // same meaning
				start_char = 'D';
				break;
			case MONTHS:
				start_char = 'B';
				break;
			case YEARS:
				start_char = 'F';
				break;
			case HOURS:
				start_char = 'H';
				break;
			case MINUTES:
				start_char = 'I';
				break;
			case SECONDS:
				start_char = 'C';
				break;
			default:
			}
		}else{
			// null start date
			// L: unknown, A: active
			// default to unknown
			start_char = 'L';
		}

		if( end_char != 0 && start_char != 0 )
			return new String(new char[]{end_char,start_char});
		else if( end_char != 0 )
			return new String(new char[]{end_char});
		else if( start_char != 0 )
			return new String(new char[]{start_char});
		else return null; // should not happen
	}

	/**
	 * For decoding instructions, see the i2b2 documentation CRC_Design.pdf
	 * The vital cd can be one or two characters.
	 * This implementation is more failsafe by using the following 
	 * algorithm:
	 * <ol>
	 *  <li>For {@code null} or {@code ""} use both timestamps accurate to day
	 *  <li>Try to decode first character as end indicator</li>
	 *  <li>If {@code vital_cd.length > 1} use second character as start indicator, otherwise if unable to decode the end indicator, use the first character.</li>
	 * </ol>
	 * @param vital_cd code to indicate accuracy of start and end date
	 */
	public void setActiveStatusCd(String vital_cd){
		Visit visit = this;
		// load accuracy
		char endIndicator = 0;
		char startIndicator = 0;
		if( vital_cd == null || vital_cd.length() == 0 ){
			// start and end date accurate to day
			// leave indicators at 0/null
		}else{
			// load first indicator character
			endIndicator = vital_cd.charAt(0);
		}
		
		ChronoUnit accuracy = null;
		
		// end date indicator
		switch( endIndicator ){
		case 'U': // unknown, no date
		case 'O': // ongoing, no date
			// set to null
			visit.setEndTime(null);
			break;
		case 0:
		case 'Y': // known, accurate to day
			accuracy = ChronoUnit.DAYS;
			break;
		case 'M': // known, accurate to month
			accuracy = ChronoUnit.MONTHS;
			break;
		case 'X': // known, accurate to year
			accuracy = ChronoUnit.YEARS;
			break;
		case 'R': // known, accurate to hour
			accuracy = ChronoUnit.HOURS;
			break;
		case 'T': // known, accurate to minute
			accuracy = ChronoUnit.MINUTES;
			break;
		case 'S': // known, accurate to second
			accuracy = ChronoUnit.SECONDS;
			break;
		default:
			// no end indicator means accurate to day
			accuracy = ChronoUnit.DAYS;
			// no match for end date -> check for start status in first character
			startIndicator = endIndicator;
		}
		// set accuracy for end time
		if( visit.getEndTime() != null && accuracy != null ){
			visit.getEndTime().setAccuracy(accuracy);
		}
		// load start indicator
		if( vital_cd != null && vital_cd.length() > 1 ){
			// use second character, if available
			startIndicator = vital_cd.charAt(1);
		}// otherwise, the first character is used if end indicator wasn't used. See default case above
		
		accuracy = null;
		// start date indicator
		switch( startIndicator ){
		case 'L': // unknown, no date
		case 'A': // active, no date
			setStartTime(null);
			break;
		case 0: // same as D
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
		default: // default to days if unable to parse
			accuracy = ChronoUnit.DAYS;
		}
		if( visit.getStartTime() != null && accuracy != null ){
			visit.getStartTime().setAccuracy(accuracy);
		}
	}

	public String getInOutCd(){
		Visit patient = this;
		if( patient.getStatus() == null )return null;
		else switch( patient.getStatus() ){
		case Inpatient:
			return "I";
		case Outpatient:
		case Emergency: // unsupported by i2b2, map to outpatient
			return "O";
		default:
			// XXX should not happen, warning
			return null;
		}
	}

}
