package de.sekmi.histream.etl;

import java.util.ArrayList;
import java.util.List;

import de.sekmi.histream.Observation;
import de.sekmi.histream.impl.VisitImpl;

public class VisitRow extends VisitImpl implements FactRow{
	List<Observation> facts;
	
	public VisitRow(){
		facts = new ArrayList<>();
	}
	@Override
	public List<Observation> getFacts() {
		return facts;
	}

	@Override
	public String getVisitId() {
		return this.getId();
	}


}
