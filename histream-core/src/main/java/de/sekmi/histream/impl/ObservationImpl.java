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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Modifier;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;

/**
 * Implementation of {@link Observation}.
 * TODO DOM view of observation
 * @author Raphael
 *
 */
@XmlRootElement(name="fact")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({StringValue.class,NumericValue.class})
public class ObservationImpl implements Observation{
	@XmlTransient
	protected ObservationFactoryImpl factory;

	@XmlAttribute(name="patient")
	protected String patientId;

	@XmlAttribute(name="encounter")
	protected String encounterId;

	@XmlAttribute(name="provider")
	protected String providerId;

	@XmlAttribute(name="location")
	protected String locationId;

	@XmlAttribute(name="concept")
	protected String conceptId;

	@XmlTransient
	protected Value value;

	@XmlAttribute(name="start")
	protected DateTimeAccuracy startTime;

	@XmlAttribute(name="end")
	protected DateTimeAccuracy endTime;

	protected Instant sourceTimestamp;
	protected String sourceId;

	/**
	 * Modifiers
	 */
	@XmlTransient // see getModifierList / setModifierList
	protected Hashtable<String, ModifierImpl> modifiers;

	/**
	 * Array of extensions, managed by the ObservationFactory
	 */
	@XmlTransient
	protected Object[] extensions;


	/**
	 * Constructor should not be called directly. Instead, use
	 * {@link ObservationFactory#createObservation(String, String, DateTimeAccuracy)}.
	 * @param factory observation factory
	 */
	protected ObservationImpl(ObservationFactoryImpl factory){
		this.factory = factory;
	}

	/**
	 * Constructor for JAXB. Make sure to set the factory after unmarshalling.
	 */
	protected ObservationImpl(){
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
	public Iterator<Modifier> getModifiers(){
		final Iterator<ModifierImpl> iter = modifiers.values().iterator();
		return new Iterator<Modifier>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Modifier next() {
				return iter.next();
			}
		};
		//return (Iterator<Modifier>)modifiers.values().iterator();
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
	public Modifier addModifier(String modifierId, Value value) {
		ModifierImpl m = new ModifierImpl(modifierId);
		// lazy allocate modifiers
		if( modifiers == null ){
			modifiers = new Hashtable<>();
		}
		// check for duplicate assignment
		if( modifiers.containsKey(modifierId) )
			throw new IllegalArgumentException("Duplicate modifier key");
		
		m.setValue(value);

		// add to modifier list
		modifiers.put(modifierId, m);
		return m;
	}

	/**
	 * Return the number of modifiers
	 * @return number of modifiers
	 */
	public int getModifierCount(){
		if( modifiers == null )return 0;
		else return modifiers.size();
	}
	@Override
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	/**
	 * Getter for JAXB
	 * @return modifier list
	 */
	@XmlElement(name="modifier")
	protected List<ModifierImpl> getModifierList(){
		if( modifiers == null || modifiers.isEmpty() )return null;

		return new ArrayList<ModifierImpl>(modifiers.values());
	}
	protected void setModifierList(List<ModifierImpl> list){
		modifiers = new Hashtable<String, ModifierImpl>();
		for( ModifierImpl i : list ){
			modifiers.put(i.getConceptId(), i);
		}
	}

	/**
	 * Getter for JAXB
	 * @return abstract value
	 */
	@XmlElement(name="value")
	protected AbstractValue getAbstractValue(){
		if( value == null )return null;
		else return (AbstractValue)value;
	}
	protected void setAbstractValue(AbstractValue value){
		this.value = value;
	}

	
	
}
