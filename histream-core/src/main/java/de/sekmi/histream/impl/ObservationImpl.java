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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Modifier;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.ext.ExternalSourceType;

/**
 * Implementation of {@link Observation}.
 * TODO DOM view of observation
 * @author Raphael
 *
 */
@XmlRootElement(name="fact")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"abstractValue","modifierList","source"})
@XmlSeeAlso({StringValue.class,NumericValue.class})
public class ObservationImpl implements Observation, Cloneable{
	public static final String XML_NAMESPACE="http://sekmi.de/histream/ns/eav-data";
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

	// TODO make compatible with JAXB
	protected ExternalSourceType source;
	/**
	 * Modifiers
	 */
	@XmlTransient // see getModifierList / setModifierList
	protected List<ModifierImpl> modifiers;

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
	@Override
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
		if( modifiers == null ){
			// no modifiers
			return null;
		}
		
		for( Modifier m : modifiers ){
			if( m.getConceptId().equals(modifierId) )return m;
		}
		
		// not found
		return null;
	}
	
	@Override
	public Iterator<Modifier> getModifiers(){
		final Iterator<ModifierImpl> iter = modifiers.iterator();
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

	public void setFactory(ObservationFactoryImpl factory){
		this.factory = factory;
	}
	
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
	public Modifier addModifier(String modifierId, Value value) {
		// lazy allocate modifiers
		if( modifiers == null ){
			modifiers = new ArrayList<>();
		}else
		// check for duplicate assignment
		if( getModifier(modifierId) != null ){
			throw new IllegalArgumentException("Duplicate modifier key");
		}

		ModifierImpl m = new ModifierImpl(modifierId);
		m.setValue(value);

		// add to modifier list
		modifiers.add(m);
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

	/**
	 * Getter for JAXB
	 * @return modifier list
	 */
	@XmlElement(name="modifier")
	protected List<ModifierImpl> getModifierList(){
		if( modifiers == null ){
			// make sure the list exists, 
			// otherwise, JAXB unmarshal may fail as some JAXB implementation will populate this list
			// without ever calling setModifierList (e.g. in JDK8)
			modifiers = new ArrayList<>();
		}
		return modifiers;
	}
	protected void setModifierList(List<ModifierImpl> list){
		modifiers = list;
	}

	@Override
	public void replaceConcept(String newConceptId) {
		this.conceptId = newConceptId;
		this.modifiers = null;
		// TODO notify extensions of concept change
		
	}

	private static class SourceAdapter extends XmlAdapter<ExternalSourceImpl, ExternalSourceType>{

		@Override
		public ExternalSourceType unmarshal(ExternalSourceImpl v) throws Exception {
			return v;
		}

		@Override
		public ExternalSourceImpl marshal(ExternalSourceType v) throws Exception {
			if( v == null )return null;
			else if( v instanceof ExternalSourceImpl )return (ExternalSourceImpl)v;
			else return new ExternalSourceImpl(v.getSourceId(), v.getSourceTimestamp());
		}

	}

	@Override
	@XmlElement
	@XmlJavaTypeAdapter(SourceAdapter.class)
	public ExternalSourceType getSource() {
		return source;
	}

	@Override
	public void setSource(ExternalSourceType source) {
		this.source = source;
	}

	
	@Override
	public ObservationImpl clone(){
		ObservationImpl clone = new ObservationImpl(this.factory);
		clone.conceptId = this.conceptId;
		clone.encounterId = this.encounterId;
		clone.endTime = this.endTime;
		clone.locationId = this.locationId;
		clone.patientId = this.patientId;
		clone.providerId = this.providerId;
		clone.startTime = this.startTime;
		clone.value = this.value; // is immutable?
		clone.extensions = this.extensions.clone();
		if( this.modifiers != null ){
			clone.modifiers = new ArrayList<>(this.modifiers);
		}
		if( this.source != null ){
			clone.source = new ExternalSourceImpl(this.source.getSourceId(), this.source.getSourceTimestamp());
		}
		return clone;
	}
	
	/**
	 * Removes information from this observation which is already contained in the provided context
	 * @param patientId patient context
	 * @param encounterId encounter context
	 * @param startTime start time
	 * @param source source context
	 */
	public void removeContext(String patientId, String encounterId, DateTimeAccuracy startTime, ExternalSourceType source){
		if( this.patientId != null && patientId != null && this.patientId.equals(patientId) ){
			this.patientId = null;
		}
		if( this.encounterId != null && encounterId != null && this.encounterId.equals(encounterId) ){
			this.encounterId = null;
		}
		if( this.startTime != null && startTime != null && this.startTime.equals(startTime) ){
			this.startTime = null;
		}
		if( this.source != null && source != null ){
			ExternalSourceImpl s = new ExternalSourceImpl(this.source.getSourceId(), this.source.getSourceTimestamp());
			if( s.getSourceId() != null && source.getSourceId() != null && s.getSourceId().equals(source.getSourceId()) ){
				s.setSourceId(null);
			}
			if( s.getSourceTimestamp() != null && source.getSourceTimestamp() != null && s.getSourceTimestamp().equals(source.getSourceTimestamp()) ){
				s.setSourceTimestamp(null);
			}
			if( s.getSourceId() == null && s.getSourceTimestamp() == null )s = null;
			this.source = s;
		}
	}

	/**
	 * Fill some information from the provided context
	 * @param patientId patient context
	 * @param encounterId visit context
	 * @param startTime start time context
	 * @param source source context
	 */
	public void fillFromContext(String patientId, String encounterId, DateTimeAccuracy startTime, ExternalSourceType source) {
		if( this.source == null ){
			setSource(source);
		}else if( source != null ){
			if( this.source.getSourceId() == null && source.getSourceId() != null ){
				this.source.setSourceId(source.getSourceId());
			}
			if( this.source.getSourceTimestamp() == null && source.getSourceTimestamp() != null ){
				this.source.setSourceTimestamp(source.getSourceTimestamp());
			}
		}
		
		if( this.patientId == null ){
			setPatientId(patientId);
		}
		if( this.encounterId == null ){
			setEncounterId(encounterId);
		}
	
		if( this.startTime == null ){
			this.startTime = startTime;
		}
	}

	@Override
	public String toString(){
		StringBuilder b = new StringBuilder(100);
		b.append("Observation(");
		b.append("pat=");
		b.append(getPatientId());
		b.append(", ");
		b.append("eid=");
		b.append(getEncounterId());
		b.append(", ");
		b.append("cid=");
		b.append(getConceptId());
		if( hasModifiers() ){
			b.append(", nmod=");
			b.append(getModifierCount());
		}
		b.append(")");
		return b.toString();
	}
}
