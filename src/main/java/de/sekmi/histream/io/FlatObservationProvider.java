package de.sekmi.histream.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.AbstractValue;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

/**
 * 
 * @author Raphael
 *
 */
public class FlatObservationProvider extends AbstractObservationParser implements FileObservationProvider{
	private BufferedReader reader;
	private Pattern fieldSeparator;
	private Pattern metaAssignment;
	private Pattern specialConceptAssignment;
	private long lineNo;
	
	private static final int maxFields = 11;
	//static private Class<?>[] supportedExtensions = new Class<?>[]{Patient.class,Visit.class};

	private Map<String, SpecialConcept> specialConcepts;
	
	private Observation fact;
	private DateTimeAccuracy sourceDateTime;
	
	
	private static enum SpecialConcept{
		PatientNames("patient.names"),
		PatientSurname("patient.surname"),
		PatientSex("patient.sex"),
		PatientBirthDate("patient.birthdate"),
		PatientDeathDate("patient.deathdate"),
		Visit("visit");
		
		private final String id;
		SpecialConcept(String id){
			this.id = id;
		}
		
		private static SpecialConcept byId(String id){
			for( SpecialConcept c : SpecialConcept.values() ){
				if( id.equals(c.id) )return c;
			}
			return null;
		}
	}
	
	private final static class Record{
		String fields[];
		
		public Record(String fields[]){
			this.fields = new String[11];
			for( int i=0; i<fields.length; i++ ){
				if( fields[i].length() == 0 || fields[i].equals("@") )fields[i] = null;
				this.fields[i] = fields[i];
			}
		}
		public String getPatID(){return fields[0];}
		public String getVisitID(){return fields[1];}
		public String getConcept(){return fields[2];}
		public String getType(){return fields[3];}
		public String getValue(){return fields[4];}
		public String getUnits(){return fields[5];}
		public String getStartDate(){return fields[6];}
		public String getEndDate(){return fields[7];}
		public String getProvider(){return fields[8];}
		public String getLocation(){return fields[9];}
		public String getFlags(){return fields[10];}
	}
	
	public FlatObservationProvider(ObservationFactory factory, BufferedReader reader){
		super(factory);
		this.reader = reader;
		this.fieldSeparator = Pattern.compile("\\t");
		this.metaAssignment = Pattern.compile("^#@meta\\(([a-z\\.]+)\\)=(.*)$");
		this.specialConceptAssignment = Pattern.compile("^#@concept\\(([a-z\\.]+)\\)=(.*)$");
		specialConcepts = new Hashtable<>();
		fact = null;
		lineNo = 0;
	}
	
	public FlatObservationProvider(ObservationFactory factory, InputStream input){
		this(factory, new BufferedReader(new InputStreamReader(input)));
	}

	private void parseCommand(String line){
		Matcher m = metaAssignment.matcher(line);
		if( m.matches() ){
			// meta
			switch( m.group(1) ){
			case "source.id":
				setSourceId(m.group(2));
				break;
			case "source.timestamp":
				parseSourceTimestamp(m.group(2));
				this.sourceDateTime = new DateTimeAccuracy(LocalDateTime.ofInstant(sourceTimestamp, ZoneId.systemDefault()));
				break;
			case "etl.strategy":
				setEtlStrategy(m.group(2));
				break;
			default:
				throw new IllegalArgumentException("Unknown meta command in line "+lineNo+": "+line);
			}
			return;
		}
		m = specialConceptAssignment.matcher(line);
		if( m.matches() ){
			SpecialConcept s = SpecialConcept.byId(m.group(1));
			if( s == null )throw new IllegalArgumentException("Illegal special concept in line "+lineNo+": " +m.group(1));
			specialConcepts.put(m.group(2), s);
			return;
		}
		
		throw new IllegalArgumentException("Invalid command in line "+lineNo+": "+line);
	}
	
	private void specialFields(SpecialConcept special, Record record){
		// create temporary observation
		// which is only used to fill the special concepts
		DateTimeAccuracy ts;
		if( record.getStartDate() == null ){
			ts = sourceDateTime;
		}else{
			ts = DateTimeAccuracy.parsePartialIso8601(record.getStartDate());
		}
		Observation tmp = factory.createObservation(record.getPatID(), record.getConcept(), ts);
		tmp.setEncounterId(record.getVisitID());
		tmp.setSourceId(sourceId);
		tmp.setSourceTimestamp(sourceTimestamp);
		
		switch( special ){
		case PatientBirthDate:
			tmp.getExtension(Patient.class).setBirthDate(DateTimeAccuracy.parsePartialIso8601(record.getValue()));
			break;
		case PatientDeathDate:
			tmp.getExtension(Patient.class).setDeathDate(DateTimeAccuracy.parsePartialIso8601(record.getValue()));
			break;
		case PatientSex:
			Patient.Sex sex;
			switch( Character.toUpperCase(record.getValue().charAt(0)) ){
			case 'F':
			case 'W':
				sex = Sex.Female;
				break;
			case 'M':
				sex = Sex.Male;
				break;
			default:
				sex = null;
			}
			tmp.getExtension(Patient.class).setSex(sex);
			break;
		case PatientNames:
			// TODO
			break;
		case PatientSurname:
			// TODO
			break;
		case Visit:
			Visit visit = tmp.getExtension(Visit.class);
			visit.setStartTime(tmp.getStartTime());
			visit.setEndTime(tmp.getEndTime());
			break;
		default:
			break;
		
		}
	}
	
	private Value parseValue(Record record){
		if( record.getType() == null )return AbstractValue.NONE;
		Value value;
		switch( record.getType() ){
		case "dat": // date
		case "str": // string
			value = new StringValue(record.getValue());
			break;
		case "int":
		case "dec":
			value = new NumericValue(new BigDecimal(record.getValue()), record.getUnits());
			break;
		default:
			// throw error
		case "nil":
			value = AbstractValue.NONE;
		}
		
		return value;
	}
	
	private void newObservation(Record record){
		DateTimeAccuracy ts;
		if( record.getStartDate() == null ){
			// first use source timestamp
			ts = sourceDateTime;
			// later update to visit timestamp
		}else{
			ts = DateTimeAccuracy.parsePartialIso8601(record.getStartDate());
		}
		fact = factory.createObservation(record.getPatID(), record.getConcept(), ts);
		
		if( ts == sourceDateTime ){
			// try to use visit timestamp
			ts = fact.getExtension(Visit.class).getStartTime();
			if( ts != null )fact.setStartTime(ts);
		}
		// set other fields
		
		fact.setEncounterId(record.getVisitID());
		fact.setSourceId(sourceId);
		fact.setSourceTimestamp(sourceTimestamp);
		fact.setValue( parseValue(record) );
		// TODO set remaining fields
		record.getEndDate();
		record.getLocation();
		record.getProvider();
		record.getFlags();
	}
	
	private void appendModifier(Record record){
		// TODO compare patid, encounter, timestamp
		// parse and add value
		Value value = parseValue(record);
		fact.addModifier(record.getConcept()).setValue(value);
	}
	
	@Override
	public Observation get() {
		String line;
		boolean inGroup = false;
		do{
			try {
				 line = reader.readLine();
				 lineNo ++;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			if( line == null ){
				// end of stream
				return null; 
			}else if( line.length() == 0 ){
				// empty line
				// continue;
			}else if( line.charAt(0) == '#' ){
				// comment or command
				if( line.length() > 1 && line.charAt(1) == '@' ){
					// command
					if( line.equals("#@group(start)") ){
						inGroup = true;
					}else if( line.equals("#@group(end)") ){
						inGroup = false;
						// resulting observation in 'fact'
						break;
					}else{ 
						parseCommand(line);
					}
					// continue;
				}else{
					// comment, ignore line
					// continue;
				}
			}else{
				// parse observation
				Record fields = new Record(fieldSeparator.split(line, maxFields));
				// fields: 0 patid, 1 encounter, 2 concept, 3: type, 4: value, 5: starttime, 
				
				// handle special concepts (defined by previous commands)
				SpecialConcept special = specialConcepts.get(fields.getConcept());
				if( special != null ){
					specialFields(special, fields);
					// continue;
				}else if( inGroup ){
					// first item is fact, following items are modifiers
					if( fact == null ){
						newObservation(fields);
					}else{
						appendModifier(fields);
					}
					// continue;
					// group ends with #@group(end)
				}else{
					// assert( fact == null )
					newObservation(fields);
					break;
				}
			}
		}while( true );
		Observation ret = fact;
		fact = null; // clear local copy
		return ret;
	}

}
