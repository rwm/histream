package de.sekmi.histream.i2b2;

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
