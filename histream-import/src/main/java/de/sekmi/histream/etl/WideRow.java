package de.sekmi.histream.etl;

import java.util.ArrayList;
import java.util.List;

import de.sekmi.histream.Observation;

public class WideRow {
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
	public List<Observation> getFacts(){
		return facts;
	}
	public String getPatientId(){return patid;}
	public String getVisitId(){return visit;}
}
