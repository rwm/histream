package de.sekmi.histream;

public interface Modifier extends ConceptValuePair{
	@Override 
	String getConceptId();
	
	@Override 
	Value getValue();
	
	void setValue(Value value);

}
