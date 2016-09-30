package de.sekmi.histream.scripting;

import java.math.BigDecimal;
import java.text.ParseException;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

public class Fact {

	private Observation observation;
	
	Fact(Observation observation){
		this.observation = observation;
	}
	
	public String getConcept(){
		return observation.getConceptId();
	}
	
	public Fact start(String incompleteDateTime) throws ParseException{
		observation.setStartTime(DateTimeAccuracy.parsePartialIso8601(incompleteDateTime));
		return this;
	}
	public Fact end(String incompleteDateTime) throws ParseException{
		observation.setEndTime(DateTimeAccuracy.parsePartialIso8601(incompleteDateTime));
		return this;
	}
	
	public StringValue value(String value){
		if( value == null ){
			observation.setValue(null);
			return null;
		}
		StringValue v = new StringValue(value);
		observation.setValue(v);
		return v;
	}
	
	/**
	 * Javascript treats all values as double, hence this method
	 * will be used by javascript calls using numbers.
	 * @param value value
	 * @return value object
	 */
	public NumericValue value(Double value){
		if( value == null ){
			observation.setValue(null);
			return null;
		}
		NumericValue v = new NumericValue(new BigDecimal(value));
		observation.setValue(v);
		return v;
	}
	
	public Observation getObservation(){
		return observation;
	}
}
