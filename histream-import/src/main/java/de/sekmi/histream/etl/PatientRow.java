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
	// TODO concepts
	private List<Observation> facts;

	public PatientRow(){
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
