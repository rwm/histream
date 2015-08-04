package de.sekmi.histream.etl;

import java.util.ArrayList;
import java.util.List;

import de.sekmi.histream.Observation;

public class WideRow implements FactRow{
	private String patid;
	private String visit;
	private List<Observation> facts;
	
	public WideRow(String patid, String visit){
		this.visit = visit;
		this.patid = patid;
		this.facts = new ArrayList<>();
	}
	
	public void addFact(Observation o){
		this.facts.add(o);
	}
	@Override
	public List<Observation> getFacts(){
		return facts;
	}
	@Override
	public String getPatientId(){return patid;}
	@Override
	public String getVisitId(){return visit;}
}
