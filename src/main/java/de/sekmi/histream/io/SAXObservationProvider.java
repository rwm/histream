package de.sekmi.histream.io;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.sekmi.histream.AbnormalFlag;
import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Modifier;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationParser;
import de.sekmi.histream.Value;
import de.sekmi.histream.Value.Type;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.AbstractObservationProvider;
import de.sekmi.histream.impl.AbstractValue;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

/**
 * Sax2 content handler which reads an xml eav representation 
 * and provides a stream of observations as the xml is processed.
 * <p>
 * Very large files can be processed, as the observations are processed sequentially when
 * they are read.
 * 
 * @author Raphael
 *
 */
public class SAXObservationProvider extends AbstractObservationProvider implements ContentHandler, ObservationParser{
	private static final Logger log = Logger.getLogger(SAXObservationProvider.class.getName());

	static private Class<?>[] supportedExtensions = new Class<?>[]{Patient.class,Visit.class};
	// TODO: also support Concept
	private Consumer<Visit> beforeFacts;
	
	private enum Section { Root, Meta, Visit, Data };
	private Section section;
	private CharBuffer buffer;
	private ObservationFactory factory;
	
	// meta
	private Instant sourceTimestamp;
	private String sourceId;
	
	// visit
	private DateTimeAccuracy encounterStart;
	private DateTimeAccuracy encounterEnd;
	private String patientId;
	private Map<String,String> visitData;
	
	private Modifier modifier;
	private Observation fact;
	// value attributes
	private Value.Type factType;
	private String valueUnit;
	private AbnormalFlag valueFlag;
	private Value.Operator valueOp;
	
	private Visit visit;
	private Patient patient;
	
	/*// no need for fast access, reading from file is slow anyways
	private ExtensionAccessor<Visit> visit;
	private ExtensionAccessor<Patient> patient;
	*/
	
	public SAXObservationProvider(ObservationFactory factory) {
		buffer = CharBuffer.allocate(1024);
		visitData = new HashMap<>();
		this.factory = factory;
		this.beforeFacts = null;
		factory.getExtensionAccessor(Patient.class);
		// TODO: assert that the supportedExtensions are available from the factory
	}
	
	@Override
	public void characters(char[] ch, int start, int length)throws SAXException {
		try{
			buffer.put(ch, start, length);
		}catch( BufferOverflowException e ){
			throw new SAXException(e);
		}
	}

	private Value parseValue(){
		AbstractValue val;
		switch( factType ){
		case None:
			if( buffer.hasRemaining() )
				log.warning("Value type None, but content not empty");
			val = AbstractValue.NONE;
			break;
		case Text:
			val = new StringValue(buffer.toString());
			break;
		case Numeric:
			val = new NumericValue(new BigDecimal(buffer.toString()), valueOp);
			break;
		default:
			throw new IllegalArgumentException("Unsupported fact type "+factType);
		}
		val.setAbnormalFlag(valueFlag);
		// TOOD: set operator
		return val;
	}
	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName)throws SAXException {
		buffer.flip();
		switch( section ){
		case Data:
			if( qName.equals("eav-item") || qName.equals("eav-group") ){
				// process observation
				if( qName.equals("eav-item") ){
					fact.setValue(parseValue());
				}
				provideObservation(fact);
				fact = null;
			}else if( qName.equals("value") ){
				// modifier value parsed
				modifier.setValue(parseValue());
			}
		case Meta:
			if( qName.equals("meta") ){
				section = Section.Root;
			}else if( qName.equals("enum") ){
				// TODO: create enum concept and store in hashtable
			}
			break;
		case Visit:
			if( qName.equals("name") ){
				// concatenate multiple names with spaces
				String names = visitData.get(qName);
				if( names == null )names = buffer.toString();
				else names = names + " " + buffer.toString();
				visitData.put(qName, names);
			}else{
				// all other fields are stored with the corresponding element name
				visitData.put(qName, buffer.toString());
			}
			break;
		case Root:
			// nothing
			break;
		}
		buffer.clear();
		
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)throws SAXException {}

	@Override
	public void processingInstruction(String target, String data)throws SAXException {}

	@Override
	public void setDocumentLocator(Locator locator) {}

	@Override
	public void skippedEntity(String name) throws SAXException {}

	@Override
	public void startDocument() throws SAXException {
		section = Section.Root;
		visitData.clear();
	}
	
	private void parseValueType(String type){
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
	private void newObservation(Attributes atts){
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
			if( beforeFacts != null ){
				beforeFacts.accept(visit);
			}
			
		}else{
			fact.setExtension(Visit.class, visit);
		}
		
		// use end time, if specified
		ts = atts.getValue("end");
		if( ts != null ){
			fact.setEndTime(DateTimeAccuracy.parsePartialIso8601(ts));
		}
		
	}

	private void parseValueAttributes(Attributes atts){
		parseValueType(atts.getValue("type"));
		valueUnit = atts.getValue("unit");
		if( valueUnit != null && valueUnit.trim().length() == 0 )valueUnit = null;
		// TODO parse abnormalFlag and operator
		valueFlag = null;
		valueOp = null;
		//
		
	}
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		buffer.clear();
		switch( section ){
		case Data:
			if( qName.equals("eav-item") ){
				newObservation(atts);
				parseValueAttributes(atts);
			}else if( qName.equals("eav-group") ){
				newObservation(atts);
				fact.setValue(AbstractValue.NONE);
			}else if( qName.equals("value") ){
				modifier = fact.addModifier(atts.getValue("modifier"));
				parseValueAttributes(atts);
			}
			break;
		case Meta:
			if( qName.equals("source") ){
				sourceTimestamp = javax.xml.bind.DatatypeConverter.parseDateTime(atts.getValue("timestamp")).toInstant();
				sourceId = atts.getValue("system");
			}
			break;
		case Visit:
			if( qName.equals("encounter") ){
				encounterStart = DateTimeAccuracy.parsePartialIso8601(atts.getValue("start"));
				if( atts.getValue("end") != null ){
					encounterEnd = DateTimeAccuracy.parsePartialIso8601(atts.getValue("end"));
				}
			}else if( qName.equals("facts") ){
				// visit section is complete
				patientId = visitData.get("patid");
				
				
				// patient and visit objects are created, once the first fact is complete
				// data section begins
				section = Section.Data;
			}
			break;
		case Root:
			if( qName.equals("meta") ){
				section = Section.Meta;
			}else if( qName.equals("visit") ){
				section = Section.Visit;
			}
			break;
		default:
			break;
		}
		
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {}

	@Override
	public Class<?>[] getSupportedExtensions() {
		return supportedExtensions;
	}
	
	/**
	 * Receive notification before facts are provided. This can be used e.g. to delete 
	 * previous data.
	 * @param handler functional interface
	 */
	public void beforeFacts(Consumer<Visit> handler){
		this.beforeFacts = handler;
	}

	@Override
	public void parse(InputStream input) throws IOException{
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(this);
			reader.parse(new InputSource(input));
		} catch (SAXException e) {
			throw new IOException(e);
		}
	}
}
