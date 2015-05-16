package histream.hl7filter;

import histream.hl7.Message;
import histream.hl7.filter.MessageFilter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Correct nonstandard messages sent by IMESO / ICUData:
 * Filters all messages with MSH.SendingFacility=ICU-DATA
 * 1. OBX.CodingSystem is set to IMDD. The software usually ignores coding systems and sends various codes, e.g. same identifier with different coding systems.
 * 2. Correct identifier, description and sub-ids. The received actually is the description truncated to N characters. Replace identifier with description, use identifiers sub-component as modifier code / sub-concept, generate valid ObservationSub-IDs (by comparing previous identifier and modifier to next obx, new values start with empty modifiers) 
 * 3. Drop OBX segments with modifier "VorlagenId" (which is an id identifying values selected from dropdown-boxes)
 * 4. Generate location information from OBR/PV1 segment and store location in OBX
 * 5. TODO: recode provider ids
 * 
 * @author marap1
 *
 */
public class ImesoCleanupFilter implements MessageFilter {

	private String sendingFacility;
	//String prevId;
	//int prevSubId;
	
	private static class CleaningState{
		String prevId;
		int prevSubId;
		
		public CleaningState(){
			this.prevId = "";
			this.prevSubId = 0;
		}
	}
	protected boolean cleanOBX(Message message, int index, CleaningState state){
		
		String[] obx = message.getSegment(index);
		
		
		// split obx[3] by encodingChars[0]
		String[] oid = message.splitFieldComponents(obx[3]);
		String[] id = message.splitSubComponents(oid[0]);

		/* new imeso forwarder is even more confusing:
		 * sub-id mixes numeric and text values. incrementing numeric values are set for every concept
		 * while subconcepts use text values in subid
		 */
		/* remember whether this OBX is a sub component */
		boolean isSubComponent = ( id.length > 1 && id[1] != null && !Character.isDigit(id[1].charAt(0)) ); 

		
		/* Replace observation id with observation text.
		 * Internally, IMESO ignores the observation id
		 * and uses the text only (due to more information
		 * detail).
		 * For outgoing messages, IMESO sets the id to the first
		 * twelve characters of the text, which looses detail
		 * due to truncation.
		 */
		
		/* 
		 * Overwrite only the first subid with the observation text
		 * in order to keep the remaining subids. 
		 */
		id[0] = oid[1];

		// remove numeric subid for non-sub-components
		if( !isSubComponent && id.length > 1 )id = new String[]{id[0]};
		
		oid[0] = message.joinSubComponents(id);
		
		if( id[0].equals(state.prevId) && isSubComponent ){
			/* reuse previous subid */
		}else{
			/* new subid */
			state.prevSubId ++;
		}
		obx[4] = Integer.toString(state.prevSubId);

		// remember previous id
		state.prevId = id[0];

		
		/* Set coding system always to IMDD.
		 * Internally, IMESO uses only observation text
		 * and ignores the coding system.
		 * 
		 * This fixes bugs, when the same identifier
		 * is sent with different coding systems (which
		 * happens in some cases)
		 */
		oid[2] = "IMDD";
		
		
		// replace non-standard value type SM with text type
		if( obx[2].equals("SM") )obx[2] = "TX";
		
		obx[3] = message.joinFieldComponents(oid);
		
		if( isSubComponent && id[1].equals("VorlagenId") )
			return false; // drop segment
		
		return true; // keep segment
		
	}

	private String[] locationFromOBR(Message message, String[] obr){
		String[] serviceid = null;
		String[] info;
		if( obr.length>4 ){
			 serviceid = message.splitFieldComponents(obr[4]);
			
			if( serviceid[0].equals("MedDeviceDat") ){
				/* geraet, patient ist genau dort, wo gerät ist! übernehme location */
				if( obr.length>13 ){
					info = message.splitFieldComponents(obr[13]);
					return new String[]{info[3],info[4],info[5]};
				}
			}else if( serviceid[0].equals("ICUFILES") ){
				/* manuelle eingabe, ort des eingebenden  */
			}
		}
		return null;
	}
	public String[] orcForOBR(Message message, int index, String[] prevOrc){
		String[] obr = message.getSegment(index);
		String[] info;
		String[] serviceid;
		String[] enteringLocation = null;
		
		if( obr.length>4 ){
			serviceid = message.splitFieldComponents(obr[4]);
			
			if( serviceid[0].equals("ICUFILES") ){
				/* manuelle eingabe, ort des eingebenden  */
			}else if( serviceid[0].equals("MedDeviceDat") ){
				/* geraet, patient ist genau dort, wo gerät ist! �bernehme location */
			}
		}
		if( obr.length>13 ){
			info = message.splitFieldComponents(obr[13]);
			if( Character.isDigit(info[3].charAt(0)) ){
			//	System.out.println("Keine richtige Station");
			}
			
			/* location */
//			System.out.println();
			enteringLocation = new String[]{info[3],info[4],info[5]};
//			System.out.println(message.joinFieldComponents(enteringLocation));
			// TODO: prepend ORC segment containing entering device[18], enterers location[13], entered by[10]
		}
		return new String[]{"ORC","OK",obr[2],obr[3],"","","","","","","","","",message.joinSubComponents(enteringLocation)};
	}
	


	@Override
	public void filter(Message message) {
		
		int pvsegid = -1;
		String[] pvlocation = null;
		int numDeletes = 0;
		
		CleaningState state = new CleaningState();
		
		for( int i=1; i<message.numSegments(); i++ ){
			String id = message.getSegmentId(i);
			if( id.equals("OBX") ){
				// clean OBX segment
				boolean keep = cleanOBX(message,i,state);
				if( keep == false ){
					// mark segment for deletion by setting segment id to null
					message.getSegment(i)[0] = null;
					numDeletes ++;
				}
				
			}else if( id.equals("OBR") ){
				String[] loc = locationFromOBR(message, message.getSegment(i));
				if( loc != null ){
					boolean update = false;
					if( pvlocation == null || pvlocation.length == 0 ){
						// replace missing PV1 location with information from first OBR segment
						// set missing or less detailed PV1 location to first location found in OBR segment
						pvlocation = loc;
						update = true;
					}else if( pvlocation.length < loc.length && pvlocation[0].equals(loc[0]) ){
						// OBR location contains more components, override PV1 location!
						pvlocation = loc;
						update = true;
					}else{
						// sync PV1 location with potentially more detailed location from OBR segment
						for( int j=0; j<loc.length; j++ ){
							if( pvlocation[j].length() == 0 ){
								pvlocation[j] = loc[j];
								update = true;
							}else if( loc[j].equals(pvlocation[j]) )continue;
							else break;
						}
					}
					if( update ){
						message.getSegment(pvsegid)[3] = message.joinFieldComponents(pvlocation);
						update = false;
					}
				}
			}else if( id.equals("PV1") ){
				pvsegid = i;
				String[] pv1 = message.getSegment(i);
				if( pv1.length <= 3 ){
					// make sure field 4 is accessible (assigned location)
					pv1 = Arrays.copyOf(pv1, 4);
					// fill padded nulls with ""
					for( int j=0; j<pv1.length; j++ ){
						if( pv1[j] == null )pv1[j]="";
					}
				}
				
				// field 4 accessible but empty
				pvlocation = message.splitFieldComponents(pv1[3]);
			}
		}
		if( numDeletes > 0 ){
			Iterator<String[]> iterator = message.getSegments().iterator();
			while( iterator.hasNext() ){
				String[] seg = iterator.next();
				if( seg[0] == null )iterator.remove();
			}
		}
		
	}
	
	@Override
	public int previewMSH(String[] msh) {
		if( msh[4].equals(this.sendingFacility) )return 1;
		else return 0;
	}

	
	protected void activate(Map<String,Object> prefs){
		Object fac = prefs.get("mshFacility");
		if( fac == null )fac = "ICU-DATA";
		this.sendingFacility = fac.toString();
	}
	
	protected void deactivate(){
		
	}
}
