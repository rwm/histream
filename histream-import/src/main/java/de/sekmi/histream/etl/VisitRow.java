package de.sekmi.histream.etl;

import java.util.List;

import de.sekmi.histream.Observation;
import de.sekmi.histream.impl.VisitImpl;

public class VisitRow extends VisitImpl implements FactRow{

	@Override
	public List<Observation> getFacts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVisitId() {
		return this.getId();
	}


}
