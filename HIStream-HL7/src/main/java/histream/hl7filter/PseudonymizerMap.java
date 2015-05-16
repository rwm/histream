package histream.hl7filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * This class provides false virtual identities for real patients, encounters, locations and visits.
 * Last names as well as male and female first names are loaded from text files.
 * TODO: implement storing and loading of mapping tables to resume
 * @author marap1
 *
 */
public class PseudonymizerMap{

	private int patientCount;
	private int locationCount;
	private int encounterCount;
	private SimpleDateFormat hl7bdate;
	
	public class VirtualPatient{
		String lastname;
		String firstname;
		String birthdate;
		boolean female;
		String patid;
		
		public String getPatID(){return patid;}
		public boolean isFemale(){return female;}
		public String getBirthdate(){return birthdate;}
		public String getLastname(){return lastname;}
		public String getFirstname(){return firstname;}
	}
	
	private HashMap<String, VirtualPatient> patientMap;
	private HashMap<String, String> locationMap;
	private HashMap<String, String> encounterMap;
	
	private String[] lastnames;
	private String[] firstnamesFemale;
	private String[] firstnamesMale;
	
	private Random rand;
	private File configDir;
	
	
	private void loadVirtualIdentities() throws IOException{
		lastnames = loadFileLines(new File(configDir,"lastnames.txt"));
		firstnamesFemale = loadFileLines(new File(configDir,"firstnames-f.txt"));
		firstnamesMale = loadFileLines(new File(configDir,"firstnames-m.txt"));
	}
	
	private VirtualPatient generatePatient(){
		VirtualPatient p = new VirtualPatient();
		patientCount ++;
		
		p.lastname = lastnames[rand.nextInt(lastnames.length)];

		p.female = rand.nextBoolean();
		String[] firstnames = p.female?firstnamesFemale:firstnamesMale;
		p.firstname = firstnames[rand.nextInt(firstnames.length)];
		p.patid = new Long(1000000+patientCount).toString();
		long epoch = (long)(rand.nextDouble()*System.currentTimeMillis());
		p.birthdate = hl7bdate.format(new Date(epoch));
		
		return p;
	}
	
	private String[] loadFileLines(File file) throws IOException{
		String charset = "ISO-8859-1";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName(charset)));
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		while( (line = reader.readLine()) != null ){
			// ignore comments and empty lines
			if( line.length() < 1 || line.charAt(0)=='#' )continue;
			
			lines.add(line);
		}
		reader.close();
		return lines.toArray(new String[]{});
	}
	
	public PseudonymizerMap(File configDir) throws IOException{
		this.configDir = configDir;
		rand = new Random();
		patientMap = new HashMap<String, VirtualPatient>();
		locationMap = new HashMap<String, String>();
		encounterMap = new HashMap<String, String>();
		hl7bdate = new SimpleDateFormat("yyyyMMdd");		
		loadVirtualIdentities();

		
	}

	public VirtualPatient getPatientPseudonym(String patient_id){
		if( patient_id == null )return null;
		VirtualPatient p = patientMap.get(patient_id);
		if( p == null ){
			p = generatePatient();
			patientMap.put(patient_id, p);
		}
		return p;
	}
	public String getEncounterPseudonym(String encounter){
		if( encounter == null )return null;
		String p = encounterMap.get(encounter);
		if( p == null ){
			p = Integer.toString(encounterCount ++);
			encounterMap.put(encounter, p);
		}
		return p;
	}
	public String getLocationPseudonym(String location){
		if( location == null )return null;
		String p = locationMap.get(location);
		if( p == null ){
			p = Integer.toString(100+(++locationCount));
			locationMap.put(location, p);
		}
		return p;
	}

}
