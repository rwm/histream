package de.sekmi.histream.impl;

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


import java.time.Instant;
import java.util.Enumeration;
import java.util.Hashtable;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Modifier;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;

public class ObservationImpl implements Observation{
	protected ObservationFactoryImpl factory;
	
	protected String patientId;
	protected String encounterId;
	protected String providerId;
	protected String locationId;
	protected String conceptId;
	protected Value value;
	protected DateTimeAccuracy startTime;
	protected DateTimeAccuracy endTime;
	
	protected Instant sourceTimestamp;
	protected String sourceId;
	
	/**
	 * Modifiers
	 */
	protected Hashtable<String, Modifier> modifiers;
	
	/**
	 * Array of extensions, managed by the ObservationFactory
	 */
	protected Object[] extensions;
	
	
	/**
	 * Constructor should not be called directly. Instead, use
	 * {@link ObservationFactory#createObservation(String, String, DateTimeAccuracy)}.
	 * @param factory observation factory
	 */
	protected ObservationImpl(ObservationFactoryImpl factory){
		this.factory = factory;
	}
	
	@Override
	public String getPatientId() {return patientId;}
	public void setPatientId(String patientId){this.patientId = patientId;}

	@Override
	public String getEncounterId() {return encounterId;}
	
	@Override
	public void setEncounterId(String encounterId){this.encounterId = encounterId;}

	@Override
	public String getProviderId() {return providerId;}

	@Override
	public String getLocationId() {return locationId;}

	@Override
	public String getConceptId() {return conceptId;}
	public void setConceptId(String conceptId){this.conceptId = conceptId;}

	@Override
	public Value getValue() {return value;}
	@Override
	public void setValue(Value value){this.value = value;}

	@Override
	public DateTimeAccuracy getStartTime() {return startTime;}
	@Override
	public void setStartTime(DateTimeAccuracy startTime){this.startTime = startTime;}

	@Override
	public DateTimeAccuracy getEndTime() {return endTime;}
	@Override
	public void setEndTime(DateTimeAccuracy endTime){this.endTime = endTime;}

	@Override
	public boolean hasModifiers() {return modifiers != null && modifiers.size() != 0;}

	@Override
	public Modifier getModifier(String modifierId) {
		if( modifiers != null )return modifiers.get(modifierId);
		else return null;
	}
	
	@Override
	public Enumeration<Modifier> getModifiers(){
		return modifiers.elements();
	}
	
	/**
	 * Assign a modifier to the concept. This will overwrite any existing modifier
	 * with the same modifierId.
	 * 
	 * @param modifier modifier
	 */
	public void setModifier(Modifier modifier){
		// lazy create hashtable only, when a modifier is set
		if( modifiers == null )modifiers = new Hashtable<>();
		// potentially overwriting existing modifier
		modifiers.put(modifier.getConceptId(), modifier);
	}

	@Override
	public ObservationFactory getFactory() {return factory;}

	@Override
	public <T> T getExtension(Class<T> extensionType) {
		// delegate to factory
		return factory.getExtension(this, extensionType);
	}

	@Override
	public <T> void setExtension(Class<T> extensionType, T extension) {
		// delegate to factory
		factory.setExtension(this, extensionType, extension);
	}

	@Override
	public Instant getSourceTimestamp() {
		return sourceTimestamp;
	}

	@Override
	public void setSourceTimestamp(Instant instant) {
		this.sourceTimestamp = instant;
	}

	@Override
	public String getSourceId() {
		return sourceId;
	}

	@Override
	public void setSourceId(String sourceSystemId) {
		this.sourceId = sourceSystemId;
	}

	@Override
	public Modifier addModifier(String modifierId) {
		ModifierImpl m = new ModifierImpl(modifierId);
		// lazy allocate modifiers
		if( modifiers == null ){
			modifiers = new Hashtable<>();
		}
		// check for duplicate assignment
		if( modifiers.containsKey(modifierId) )
			throw new IllegalArgumentException("Duplicate modifier key");
		
		// add to modifier list
		modifiers.put(modifierId, m);
		return m;
	}

	@Override
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	
	
}
