package histream.hl7filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import histream.hl7.Message;
import histream.hl7.filter.MessageFilter;

/**
 * Filter HL7v2 messages with regular expressions.
 * Example <pre>{@code
   Substitute text in field 8 of PID segment (replace W with F)
	regex:PID:8:s/W/F/
	subst:PID:8:|W|F|

   Substitute non-standard type SM with TX in OBX field 2
    OBX:2:s/SM/TX/


 * }</pre>
 * @author marap1
 *
 */
public class RegexFilter implements MessageFilter{
	private static Logger log = Logger.getLogger(RegexFilter.class.getName());
	private ArrayList<SegmentFilter> filters;
	private HashMap<String, int[]> segmentFilters;

	private static final String PREF_FILTERS="filters";
	
	public RegexFilter(){
		filters = new ArrayList<SegmentFilter>();
		segmentFilters = new HashMap<String, int[]>();
	}
	
	
	@Override
	public void filter(Message message) {
		
		for( String[] segment : message.getSegments() ){
			int[] f = segmentFilters.get(segment[0]);
			if( f == null )continue;
			for( int id : f ){
				filters.get(id).filter(segment);
			}
		}
	}

	/**
	 * Add a segment filter
	 * @param filter filter to add
	 */
	private void addSegmentFilter(String filterString){
		// parse filter
		String[] parts = filterString.split(":",4);
		if( parts.length < 3 ){
			log.severe("Ignoring invalid filter string (too short): "+filterString);
			return;
		}
		String segment = parts[1];
		int field = -1;
		try{
			if( parts[2].length() == 0 )throw new NumberFormatException();
			field = Integer.parseInt(parts[2]);
		}catch( NumberFormatException e ){
			log.severe("Ignoring filter with invalid field number: "+filterString);
			return;
		}
		String filter = parts[3];
		if( parts[0].equals("regex") ){
			// parse regex filter
			if( filter.charAt(0) != 's' ){
				log.severe("Ignoring invalid filter (regex must beginn with 's'): "+filterString);
				return;
			}
			char sep = filter.charAt(1);
			int mid = filter.indexOf(sep, 2);
			int end = filter.indexOf(sep, mid+1);
			// TODO: support options specified after endSeparator
			filters.add(new RegexField(segment, field, filter.substring(2, mid), filter.substring(mid+1, end)));
			
		}else if( parts[0].equals("subst") ){
			// parse substitute filter
			char sep = filter.charAt(0);
			int mid = filter.indexOf(sep, 1);
			int end = filter.indexOf(sep, mid+1);
			// TODO: support options specified after endSeparator
			filters.add(new ReplaceField(segment, field, filter.substring(1, mid), filter.substring(mid+1, end)));
		}else{
			// report invalid filter
			log.severe("Ignoring unsupported filter: "+parts[0]);
		}
	}
	
	/**
	 * Assign all registered segment filters to a hash map for
	 * O(1) access. Filters for same segments are called in order of specification.
	 */
	private void assignSegmentFilters(){
		HashMap<String, LinkedList<Integer>> list = new HashMap<String, LinkedList<Integer>>();
		for( int i=0; i<filters.size(); i++ ){
			SegmentFilter filter = filters.get(i);
			LinkedList<Integer> a = list.get(filter.getSegmentId());
			if( a == null ){
				a = new LinkedList<Integer>();
				a.add(new Integer(i));
				list.put(filter.getSegmentId(), a);
			}else{
				a.add(new Integer(i));
			}
		}
		segmentFilters.clear();
		for( String key : list.keySet() ){
			LinkedList<Integer> l = list.get(key);
			int[] a = new int[l.size()];
			int i=0;
			for( Integer id : l ){
				a[i] = id;
				i++;
			}
			segmentFilters.put(key, a);
		}
	}
	
	@Override
	public int previewMSH(String[] msh) {
		return 1; // filter all messages
	}
	
	/**
	 * Called by OSGi framework to activate/configure component
	 * @param prefs preferences
	 */
	protected void activate(Map<String,Object> prefs){
		// load filters
		Object o = prefs.get(PREF_FILTERS);
		if( o != null && o instanceof String[] ){
			// filters in string array
			for( String filter : (String[])o ){
				addSegmentFilter(filter);
			}
		}else{
			// try to load indexed properties (fileinstall does not support multi valued properties)
			int index = 1;
			String paramPrefix = PREF_FILTERS+".";
			Object value;
			while( (value = prefs.get(paramPrefix+index)) != null ){
				addSegmentFilter(value.toString());
				index ++;
			}
		}
		assignSegmentFilters();
	}
	
	/**
	 * Called by OSGi framework to deactivate component
	 * @param prefs preferences
	 */
	protected void deactivate(){
		
	}

}
