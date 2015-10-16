package de.sekmi.histream.etl;

import java.util.Arrays;
import java.util.List;

import de.sekmi.histream.Observation;

/**
 * Row from EAV table. Per definition, only
 * a single fact per row is contained.
 * 
 * @author R.W.Majeed
 *
 */
public class EavRow implements FactRow {
	private Observation fact;
	
	public EavRow(Observation fact){
		this.fact = fact;
	}
	@Override
	public List<Observation> getFacts() {
		return Arrays.asList(fact);
	}
	public Observation getFact(){
		return fact;
	}

	@Override
	public String getPatientId() {return fact.getPatientId();}

	@Override
	public String getVisitId() {return fact.getEncounterId();}

}
