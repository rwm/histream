package de.sekmi.histream.io;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.sekmi.histream.AbnormalFlag;
import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Modifier;
import de.sekmi.histream.Observation;
import de.sekmi.histream.Value;
import de.sekmi.histream.Value.Type;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.impl.AbstractValue;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

/**
 * Parser for EAV XML documents. This class is used by both the {@link SAXObservationProvider}
 * and {@link XMLObservationProvider}.
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
	protected String providerId;
	protected String providerName;

	// visit
	protected DateTimeAccuracy encounterStart;
	protected DateTimeAccuracy encounterEnd;
	protected String patientId;
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
	
	protected Visit visit;
	protected Patient patient;

	
	protected XMLObservationParser(){
		visitData = new HashMap<>();
		//factory.getExtensionAccessor(Patient.class);
		// TODO: assert that the supportedExtensions are available from the factory
	}
	
	protected void parseValueType(String type){
		if( type == null ){
			factType = Type.None;
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


		fact = factory.createObservation(patientId, atts.getValue("concept"), start);
		// set encounter id to visitData.get("encounter")
		fact.setEncounterId(visitData.get("encounter"));

		// set source information
		fact.setSourceId(sourceId);
		fact.setSourceTimestamp(sourceTimestamp);
		
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
		parseSourceTimestamp(atts.getValue("timestamp"));
		sourceId = atts.getValue("source");
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
	 * @param valueText
	 * @return new value which also contains value attributes
	 */
	protected Value parseValue(String valueText){
		AbstractValue val;
		switch( factType ){
		case None:
			if( valueText.length() != 0 )
				log.warning("Value type None, but content not empty");
			val = AbstractValue.NONE;
			break;
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
		// TOOD: set operator
		return val;
	}
}
