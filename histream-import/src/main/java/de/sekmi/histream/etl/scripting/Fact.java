package de.sekmi.histream.etl.scripting;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.Value;
import de.sekmi.histream.impl.StringValue;

public class Fact {

	private Observation observation;
	
	Fact(Observation observation){
		this.observation = observation;
	}
	
	public String getConcept(){
		return observation.getConceptId();
	}
	
	public Fact start(String incompleteDateTime){
		observation.setStartTime(DateTimeAccuracy.parsePartialIso8601(incompleteDateTime));
		return this;
	}
	public Fact end(String incompleteDateTime){
		observation.setEndTime(DateTimeAccuracy.parsePartialIso8601(incompleteDateTime));
		return this;
	}
	
	public Value value(String value){
		Value v = new StringValue(value);
		observation.setValue(v);
		return v;
	}
	
	public Observation getObservation(){
		return observation;
	}
}
