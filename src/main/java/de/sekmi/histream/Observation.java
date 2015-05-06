package de.sekmi.histream;

import java.util.Enumeration;

import de.sekmi.histream.ext.ExternalSourceType;


public interface Observation extends ConceptValuePair, ExternalSourceType{
	String getPatientId();
	String getEncounterId();
	String getProviderId();
	String getLocationId();
	
	@Override 
	String getConceptId();
	
	@Override 
	Value getValue();
	void setValue(Value value);
	
	DateTimeAccuracy getStartTime();
	DateTimeAccuracy getEndTime();
	void setEndTime(DateTimeAccuracy date);
	
	ObservationFactory getFactory();
	<T> T getExtension(Class<T> extensionType) throws IllegalArgumentException;
	<T> void setExtension(Class<T> extensionType, T extension) throws IllegalArgumentException;
	
	void setEncounterId(String encounterId);
	
	boolean hasModifiers();
	Modifier getModifier(String modifierId);
	Enumeration<Modifier> getModifiers();
	Modifier addModifier(String modifierId)throws IllegalArgumentException;
}
