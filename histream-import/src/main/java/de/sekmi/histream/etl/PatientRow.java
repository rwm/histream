package de.sekmi.histream.etl;

import java.util.ArrayList;
import java.util.List;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.impl.PatientImpl;

/**
 * Row of patient data
 * TODO: implement {@link Patient}
 * @author Raphael
 *
 */
public class PatientRow extends PatientImpl implements FactRow{
	String firstname;
	String lastname;
	// TODO concepts
	List<Observation> facts;
	

	public void setNames(String first, String last){
		this.firstname = first;
		this.lastname = last;
		this.facts = new ArrayList<>();
	}


	@Override
	public List<Observation> getFacts() {
		return facts;
	}


	@Override
	public String getPatientId() {
		return this.getId();
	}


	@Override
	public String getVisitId() {
		// no visit id for patient facts
		return null;
	}
}
