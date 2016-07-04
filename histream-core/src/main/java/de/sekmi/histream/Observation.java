package de.sekmi.histream;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.Iterator;

import de.sekmi.histream.ext.ExternalSourceType;

/**
 * Observation of a single event or a single fact assigned to a single patient.
 * 
 * TODO more details
 * 
 * @author R.W.Majeed
 *
 */
public interface Observation extends ConceptValuePair{
	String getPatientId();
	String getEncounterId();
	String getProviderId();
	String getLocationId();
	
	@Override 
	String getConceptId();
	
	public ExternalSourceType getSource();
	public void setSource(ExternalSourceType source);
	
	/**
	 * Replace the concept id. All modifiers will be removed from the observation.
	 * @param newConceptId new concept id
	 */
	void replaceConcept(String newConceptId);
	
	@Override 
	Value getValue();
	void setValue(Value value);
	
	DateTimeAccuracy getStartTime();
	DateTimeAccuracy getEndTime();
	void setEndTime(DateTimeAccuracy date);
	void setStartTime(DateTimeAccuracy date);
	
	ObservationFactory getFactory();
	/**
	 * Access the extension for the given type.
	 * This will also allocate and assign the extension to
	 * the observation if this is the first time it is accessed
	 * for this observation.
	 * 
	 * @param <T> Extension type
	 * @param extensionType extension class.
	 * @return extension object
	 * @throws IllegalArgumentException
	 */
	<T> T getExtension(Class<T> extensionType) throws IllegalArgumentException;
	<T> void setExtension(Class<T> extensionType, T extension) throws IllegalArgumentException;
	
	void setPatientId(String patientId);
	void setEncounterId(String encounterId);
	void setLocationId(String locationId);
	
	/**
	 * Whether this observation contains sub-concepts (=modifiers).
	 * @see #getModifier(String)
	 * @return true if modifiers are present, false otherwise
	 */
	boolean hasModifiers();
	Modifier getModifier(String modifierId);
	
	/**
	 * Get all modifiers
	 * @return modifiers
	 */
	Iterator<Modifier> getModifiers();
	
	/**
	 * Add a sub concept to this observation.
	 * 
	 * Use the returned {@link Modifier} to add additional information (e.g. value)
	 * to the modifier.
	 * 
	 * @param modifierId concept id for the new modifier
	 * @param value modifier value
	 * @return new modifier
	 * @throws IllegalArgumentException if the given modifierId is already used.
	 */
	Modifier addModifier(String modifierId, Value value)throws IllegalArgumentException;
}
