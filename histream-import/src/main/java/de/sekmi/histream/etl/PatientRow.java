package de.sekmi.histream.etl;

import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.impl.PatientImpl;

/**
 * Row of patient data
 * TODO: implement {@link Patient}
 * @author Raphael
 *
 */
public class PatientRow extends PatientImpl{
	String firstname;
	String lastname;
	// TODO concepts
	

	public void setNames(String first, String last){
		this.firstname = first;
		this.lastname = last;
	}
}
