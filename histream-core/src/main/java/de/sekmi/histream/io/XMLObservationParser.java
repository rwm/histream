package de.sekmi.histream.io;

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


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.sekmi.histream.AbnormalFlag;
import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Modifier;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.Value;
import de.sekmi.histream.Value.Type;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.impl.AbstractValue;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

/**
 * Parser for EAV XML documents. This class is used by {@link XMLObservationSupplier}.
 * 
 * @author marap1
 *
 */
class XMLObservationParser extends AbstractObservationParser{
	private static final Logger log = Logger.getLogger(XMLObservationParser.class.getName());
	
	public static interface AttributeAccessor{
		String getValue(String name);
	}
	

	// provider
	//protected String providerId;
	//protected String providerName;

	// visit
	protected DateTimeAccuracy encounterStart;
	protected DateTimeAccuracy encounterEnd;
	//protected String patientId;
	protected Map<String,String> visitData;
	
	/**
	 * Last added modifier to observation
	 */
	protected Modifier modifier;
	protected Observation fact;
	// value attributes
	protected Value.Type factType;
	protected String valueUnit;

	
	protected AbnormalFlag valueFlag;
	protected Value.Operator valueOp;
	
	// cached visit for visit extension
	protected Visit visit;
	// cached patient for patient extension
	protected Patient patient;

	
	protected XMLObservationParser(){
		visitData = new HashMap<>();
		//factory.getExtensionAccessor(Patient.class);
		// TODO: assert that the supportedExtensions are available from the factory
	}
	
	protected void parseValueType(String type){
		if( type == null ){
			factType = null;
		}else switch( type ){
		case "xsi:string":
			factType = Type.Text;
			break;
		case "xsi:decimal":
		case "xsi:integer":
			factType = Type.Numeric;
			break;
		default:
			// TODO: log error
		}				
	}
	protected void newObservation(AttributeAccessor atts){
		// determine start time
		DateTimeAccuracy start;
		String ts = atts.getValue("start");
		if( ts != null ) // use specified time
			start = DateTimeAccuracy.parsePartialIso8601(ts);
		else // use time from encounter
			start = encounterStart;


		fact = factory.createObservation(visitData.get("patid"), atts.getValue("concept"), start);
		// set encounter id to visitData.get("encounter")
		fact.setEncounterId(visitData.get("encounter"));
		fact.setLocationId(atts.getValue("location"));
		// set source information
		fact.setSource(this);
		
		if( patient == null ){
			// this is the first observation
			// create/sync patient object
			patient = fact.getExtension(Patient.class);
			// TODO: set patient data
			DateTimeAccuracy birthDate = null, deathDate = null;
			if( visitData.containsKey("birthdate") ){
				birthDate = DateTimeAccuracy.parsePartialIso8601(visitData.get("birthdate"));
			}
			if( visitData.containsKey("deathdate") ){
				deathDate = DateTimeAccuracy.parsePartialIso8601(visitData.get("deathdate"));				
			}
			Sex sex = null;
			if( visitData.containsKey("sex") ){
				switch( visitData.get("sex").charAt(0) ){
				case 'F':
					sex = Sex.Female;
					break;
				case 'M':
					sex = Sex.Male;
					break;
				default:
					// warning
					log.warning("Unable to recognize patient sex '"+visitData.get("sex")+"': F or M expected");
				}
			}
			// sync data
			boolean overwrite = false;
			if( patient.getSourceTimestamp() == null ){
				// patient did not exist in cache,
				// set source/timestamp to this file
				patient.setSourceTimestamp(sourceTimestamp);
				// TODO: set source id
				overwrite = true;
			}else if( sourceTimestamp.isAfter(patient.getSourceTimestamp()) ){
				// patient already existing, but our information is more recent
				// overwrite existing data
				overwrite = true;
			}
			
			// XXX only for testing
			overwrite = true; // TODO rewrite overwrite/sync condition
			
			if( overwrite ){
				patient.setBirthDate(birthDate);
				patient.setDeathDate(deathDate);
				patient.setSex(sex);
				// TODO set name, surname, etc.
			}
		}else{
			fact.setExtension(Patient.class, patient);			
		}
		
		if( visit == null ){
			// this is the first observation for this visit
			// create/sync visit object
			visit = fact.getExtension(Visit.class);
			visit.setStartTime(encounterStart);
			visit.setEndTime(encounterEnd);
			visit.setLocationId(visitData.get("location"));
//			visit.setStatus(status.);
			// TODO: set visit data
			
			// notify handlers
			/*
			if( beforeFacts != null ){
				beforeFacts.accept(visit);
			}*/
			
		}else{
			fact.setExtension(Visit.class, visit);
		}
		
		// use end time, if specified
		ts = atts.getValue("end");
		if( ts != null ){
			fact.setEndTime(DateTimeAccuracy.parsePartialIso8601(ts));
		}
		
		
	}

	protected void parseSource(AttributeAccessor atts){
		setMeta(ObservationSupplier.META_SOURCE_TIMESTAMP, atts.getValue("timestamp"));
		setMeta(ObservationSupplier.META_SOURCE_ID, atts.getValue("id"));
	}
	protected void parseEncounter(AttributeAccessor atts){
		encounterStart = DateTimeAccuracy.parsePartialIso8601(atts.getValue("start"));
		if( atts.getValue("end") != null ){
			encounterEnd = DateTimeAccuracy.parsePartialIso8601(atts.getValue("end"));
		}
	}
	protected void parseValueAttributes(AttributeAccessor atts){
		parseValueType(atts.getValue("type"));
		valueUnit = atts.getValue("unit");
		if( valueUnit != null && valueUnit.trim().length() == 0 )valueUnit = null;
		// TODO parse abnormalFlag and operator
		valueFlag = null;
		valueOp = null;
		//
		
	}
	/**
	 * Parse value text according to previously parsed value attributes.
	 * Requires previous call to {@link #parseValueAttributes(AttributeAccessor)}
	 * @param valueText value string
	 * @return new value which also contains value attributes
	 */
	protected Value parseValue(String valueText){
		AbstractValue val;
		if( factType == null ){
			if( valueText != null && valueText.length() != 0 )
				log.warning("Value type None, but content not empty");
			val = null;
		}else{
			switch( factType ){
			case Text:
				val = new StringValue(valueText);
				break;
			case Numeric:
				val = new NumericValue(new BigDecimal(valueText), valueUnit, valueOp);
				break;
			default:
				throw new IllegalArgumentException("Unsupported fact type "+factType);
			}
			val.setAbnormalFlag(valueFlag);
		}
		// TOOD: set operator
		return val;
	}
}
