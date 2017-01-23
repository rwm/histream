package de.sekmi.histream.etl;

public class MapFeedback {
	private boolean drop;
	private String concept;
	private String value;
	
	public MapFeedback(){
		drop = false;
	}
	public void overrideConcept(String newConcept){
		this.concept = newConcept;
	}
	public void overrideValue(String newValue){
		this.value = newValue;
	}
	public void logWarning(String warning){
		// TODO forward to parse warning handler
		System.err.println("Map warning: "+warning);
	}
	
	// drop the fact, no observation will be generated
	public void dropFact(){
		drop = true;
	}
	
	public String getValueOverride(){
		return value;
	}
	public boolean hasConceptOverride(){
		return concept != null;
	}
	public String getConceptOverride(){
		return concept;
	}
	public boolean isActionDrop(){
		return drop;
	}

	/**
	 * Reset/clear value override
	 */
	public void resetValue(){
		this.value = null;
	}
}
