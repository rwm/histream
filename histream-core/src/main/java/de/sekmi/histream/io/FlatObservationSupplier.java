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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ExtensionAccessor;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.Value;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Patient.Sex;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

/**
 * Reads observations from flat text files.
 * 
 * <p>
 * Compatible text files are read line by line via {@link BufferedReader#readLine()}. 
 * The line separator characters are system dependent.
 * <p>
 * The file MUST start with a header line which should list the headers specified in {@link #minHeaders}. 
 * Additional column headers are allowed to the right of the minimum headers.
 * <p>
 * Each of the following lines may start with a comment character, which by default is '#'.
 * <p>
 * If the comment starts with '#@', then the line is understood as special command
 * to the reader. Allowed commands are {@code #@meta}, {@code #@concept} and {@code #@group}.
 * <p>
 * Any non-empty lines which are not comments are parsed as delimited values.
 * TODO describe colums.
 *   
 * @author Raphael
 *
 */
public class FlatObservationSupplier extends AbstractObservationParser implements ObservationSupplier{
	private static final Logger log = Logger.getLogger(FlatObservationSupplier.class.getName());
	/**
	 * Minimum headers required in first line. Additional columns to the right are ignored (warning)
	 */
	public static final String[] minHeaders = new String[]{"patid","encounter","concept","type","value","units","start","end","provider","location","flag"};
	/**
	 * Maximum number of fields (used to split fields)
	 */
	private static final int maxFields = minHeaders.length+1;
	private BufferedReader reader;
	private String fieldSeparator;
	private String commentPrefix;
	private String commandPrefix;
	private String commandGroupStart;
	private String commandGroupEnd;	
	private Pattern fieldSeparatorPattern;
	private Pattern metaAssignment;
	private Pattern specialConceptAssignment;
	private long lineNo;
	
	//static private Class<?>[] supportedExtensions = new Class<?>[]{Patient.class,Visit.class};

	private Map<String, SpecialConcept> specialConcepts;
	
	private Observation fact;
	private ExtensionAccessor<Patient> patientAccessor;
	private ExtensionAccessor<Visit> visitAccessor;
	private Patient currentPatient;
	private Visit currentVisit;
	
	/**
	 * Unprocessed line if non null (used to look ahead)
	 */
	private String prefetchLine;
	//private DateTimeAccuracy sourceDateTime;
	
	
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
	
	public FlatObservationSupplier(ObservationFactory factory, BufferedReader reader) throws IOException{
		setObservationFactory(factory);
		this.reader = reader;
		this.fieldSeparator = "\t";
		this.fieldSeparatorPattern = Pattern.compile(Pattern.quote(fieldSeparator));
		this.metaAssignment = Pattern.compile("^#@meta\\(([a-z\\.]+)\\)=(.*)$");
		this.specialConceptAssignment = Pattern.compile("^#@concept\\(([a-z\\.]+)\\)=(.*)$");
		this.visitAccessor = factory.getExtensionAccessor(Visit.class);
		this.patientAccessor = factory.getExtensionAccessor(Patient.class);
		specialConcepts = new Hashtable<>();
		fact = null;
		lineNo = 0;
		
		// TODO load from configuration
		commentPrefix = "#";
		commandPrefix = "#@";
		commandGroupStart = "#@group(start)";
		commandGroupEnd = "#@group(end)";
		
		verifyPrefixes();
		
		// read meta info
		readMeta();
	}
	
	private void verifyPrefixes()throws IOException{
		if( !commandPrefix.startsWith(commentPrefix) )throw new IOException("commandPrefix must start with commentPrefix");
		if( !commandGroupStart.startsWith(commandPrefix) || !commandGroupEnd.startsWith(commandPrefix))throw new IOException("groupStart and groupEnd must start with commandPrefix");		
	}
	
	private void readMeta() throws IOException{
		// verify headers in first line
		String headers = reader.readLine();
		String expected = String.join(fieldSeparator, minHeaders);
		if( !headers.startsWith(expected) ){
			throw new IOException("Header in first line must start with: "+expected);
		}else if( !headers.equals(expected) ){
			log.warning("Additional columns are ignored: "+headers.substring(expected.length()+1));
		}
		// parse meta commands/comments
		do{
			prefetchLine = reader.readLine(); 
			if( prefetchLine == null ){
				// end of file
				prefetchLine = null;
				break;
			}else if( prefetchLine.startsWith(commandPrefix) ){
				if( prefetchLine.startsWith(commandGroupStart) || prefetchLine.startsWith(commandGroupEnd) ){
					// group start/end
					break;
				}
				parseCommand(prefetchLine);
				// continue
			}else if( prefetchLine.length() == 0 || prefetchLine.startsWith(commentPrefix) ){
				// ignore comment or empty lines
				// continue
			}else{
				// content. stop prefetching row
				break;
			}
		}while( true );

	}
	
	public FlatObservationSupplier(ObservationFactory factory, InputStream input) throws IOException{
		// TODO: standard API functionality: close() method will not close InputStream passed in constructor
		this(factory, new BufferedReader(new InputStreamReader(input)));
	}

	private void parseCommand(String line){
		Matcher m = metaAssignment.matcher(line);
		if( m.matches() ){
			// meta
			setMeta(m.group(1), m.group(2));

			//this.sourceDateTime = new DateTimeAccuracy(LocalDateTime.ofInstant(sourceTimestamp, ZoneId.systemDefault()));
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
	
	private DateTimeAccuracy getSourceDateTime(){
		return new DateTimeAccuracy(LocalDateTime.ofInstant(sourceTimestamp, ZoneId.systemDefault()));
	}
	private void lazyCreatePatient(String patientId){
		if( currentPatient == null || !currentPatient.getId().equals(patientId) ){
			currentPatient = patientAccessor.accessStatic(patientId,this);
		}
	}
	private void lazyCreateVisit(String visitId, String patientId){
		if( currentVisit == null || !currentVisit.getId().equals(visitId) ){
			currentVisit = visitAccessor.accessStatic(visitId,currentPatient,(ExternalSourceType)this);
		}
	}
	private void specialFields(SpecialConcept special, Record record){		
		// make sure current patient is valid
		lazyCreatePatient(record.getPatID());
		
		switch( special ){
		case PatientBirthDate:
			currentPatient.setBirthDate(DateTimeAccuracy.parsePartialIso8601(record.getValue()));
			break;
		case PatientDeathDate:
			currentPatient.setDeathDate(DateTimeAccuracy.parsePartialIso8601(record.getValue()));
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
			currentPatient.setSex(sex);
			break;
		case PatientNames:
			// TODO
			break;
		case PatientSurname:
			// TODO
			break;
		case Visit:
			lazyCreateVisit(record.getVisitID(), record.getPatID());
			
			currentVisit.setStartTime(DateTimeAccuracy.parsePartialIso8601(record.getStartDate()));
			currentVisit.setEndTime(DateTimeAccuracy.parsePartialIso8601(record.getEndDate()));
			
			currentVisit.setLocationId(record.getLocation());
			// TODO set provider
			record.getProvider();
			break;
		default:
			break;
		
		}
	}
	
	private Value parseValue(Record record){
		if( record.getType() == null )return null;
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
			value = null;
		}
		
		return value;
	}
	
	private void newObservation(Record record){
		DateTimeAccuracy ts;
		DateTimeAccuracy sourceTs = getSourceDateTime();
		if( record.getStartDate() == null ){
			// first use source timestamp
			ts = sourceTs;
			// later update to visit timestamp
		}else{
			ts = DateTimeAccuracy.parsePartialIso8601(record.getStartDate());
		}
		fact = factory.createObservation(record.getPatID(), record.getConcept(), ts);
		fact.setEncounterId(record.getVisitID());
		patientAccessor.set(fact, currentPatient);
		visitAccessor.set(fact, currentVisit);
		// set other fields
		fact.setSource(this);
		
		fact.setValue( parseValue(record) );
		if( record.getEndDate() != null ){
			fact.setEndTime(DateTimeAccuracy.parsePartialIso8601(record.getEndDate()));
		}
		fact.setLocationId(record.getLocation());

		if( ts == sourceTs ){
			// try to use visit timestamp
			ts = fact.getExtension(Visit.class).getStartTime();
			if( ts != null )fact.setStartTime(ts);
		}

		
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
		try{
			fact.addModifier(record.getConcept(),value);
		}catch( IllegalArgumentException e ){
			log.severe("Unable to add modifier in line "+lineNo+": "+record.getConcept());
			// TODO FIX line numbers not incremented correctly (line 220000 lines but should be 220006)
			throw e;
		}
	}
	
	@Override
	public Observation get() {
		String line;
		boolean inGroup = false;
		do{
			try {
				if( prefetchLine != null ){
					line = prefetchLine;
					prefetchLine = null;
				}else{
					line = reader.readLine();
					lineNo ++;
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			if( line == null ){
				// end of stream
				return null; 
			}else if( line.length() == 0 ){
				// empty line
				// continue;
			}else if( line.startsWith(commandPrefix) ){
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
			}else if( line.startsWith(commentPrefix) ){
				// comment, ignore line
				// continue;
			}else{
				// parse observation
				Record fields = new Record(fieldSeparatorPattern.split(line, maxFields));
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

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
