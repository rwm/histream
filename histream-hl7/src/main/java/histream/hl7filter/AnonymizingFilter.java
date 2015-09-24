package histream.hl7filter;


import histream.hl7.filter.SegmentFilter;

import java.io.File;
import java.io.IOException;

/**
 * HL7v2 message filter which removes patient identifying information. 
 * No guaranty for completeness.
 * Segments NK1, IN1, IN2 are dropped.
 * Patient number, encounter number, location are replaced by generated numbers which are 
 * kept in memory to allow linking of of patients/encounters/locations.
 * Patients names, sex, birthdays are replaced by random values.
 * 
 * @author RWM
 *
 */
public class AnonymizingFilter extends SegmentFilter{
	PseudonymizerMap pseudo;
	private static final String EMPTY_FIELD="";
	
	public static String[] dropSegments = new String[]{"NK1","IN1","IN2"};
	
	@Override
	protected String[] filterSegment(String[] fields) {
		String segname = fields[0];
		
		// drop sensitive segments
		for( int i=0; i<dropSegments.length; i++ )
			if( segname.equals(dropSegments[i]) )return null;
		
		// rewrite identifying segments
		if( segname.equals("PV1") ){
			if( fields.length > 3 && fields[3].length() != 0 )fields[3] = pseudo.getLocationPseudonym(fields[3]);
			
			if( fields.length > 6 && fields[6].length() != 0 )fields[6] = pseudo.getLocationPseudonym(fields[6]);
			if( fields.length > 8 && fields[8].length() != 0 )fields[8] = EMPTY_FIELD;
			if( fields.length > 9 && fields[9].length() != 0 )fields[9] = EMPTY_FIELD;
			if( fields.length > 11 && fields[11].length() != 0 )fields[11] = pseudo.getLocationPseudonym(fields[11]);
			if( fields.length > 19 && fields[19].length() != 0 )fields[19] = pseudo.getEncounterPseudonym(fields[19]);
			setElementsToNull(fields, 20);
			
		}else if( segname.equals("PID") ){
			String pid = fields[2];
			if( pid == null )pid = fields[3];
			if( pid == null )pid = fields[4];
			PseudonymizerMap.VirtualPatient p = pseudo.getPatientPseudonym(pid);
			
			fields = new String[]{fields[0],fields[1],p.getPatID(),null,null,p.getLastname()+"^"+p.getFirstname(),null,p.getBirthdate(),p.isFemale()?"F":"M" };
		}else if( segname.equals("MRG") ){
			if( fields.length > 1 && fields[1].length() != 0 )fields[1] = pseudo.getPatientPseudonym(fields[1]).getPatID();		
			if( fields.length > 2 && fields[2].length() != 0 )fields[2] = pseudo.getPatientPseudonym(fields[2]).getPatID();
			if( fields.length > 3 )fields[3] = EMPTY_FIELD;
			if( fields.length > 4 && fields[4].length() != 0 )fields[4] = pseudo.getPatientPseudonym(fields[4]).getPatID();
			if( fields.length > 5 && fields[5].length() != 0 )fields[5] = pseudo.getPatientPseudonym(fields[5]).getPatID();
			if( fields.length > 6 && fields[6].length() != 0 )fields[6] = pseudo.getPatientPseudonym(fields[6]).getPatID();
			setElementsToNull(fields,7);
		}else if( segname.equals("ORC") ){
			if( fields.length > 12 && fields[12].length() != 0 )fields[12] = pseudo.getLocationPseudonym(fields[12]);
		}else if( segname.equals("OBR") ){
			if( fields.length > 16 && fields[16].length() != 0 )fields[16] = pseudo.getLocationPseudonym(fields[16]);
		}
		return fields;
	}
		
	public AnonymizingFilter(File configDir) throws IOException{
		pseudo = new PseudonymizerMap(configDir);
	}
	
	private static final void setElementsToNull(String[] a, int firstNullIndex){
		for( int i=firstNullIndex; i<a.length; i++ )a[i] = EMPTY_FIELD;
	}

	@Override
	public int previewMSH(String[] msh) {
		return 1; // process all messages
	}
}
