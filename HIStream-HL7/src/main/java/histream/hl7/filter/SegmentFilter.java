package histream.hl7.filter;

import histream.hl7.Message;

import java.util.Iterator;

/**
 * Abstract filter to simplify filtering of HL7v2 message segments.
 * 
 * @author RWM
 *
 */
public abstract class SegmentFilter implements MessageFilter {

	@Override
	public final void filter(Message message) {
		Iterator<String[]> iter = message.getSegments().iterator();
		while( iter.hasNext() ){
			String[] fields = iter.next();
			fields = filterSegment(fields);
			if( fields == null )iter.remove();
		}
	}
	
	/**
	 * Filters a segment consisting of split fields.
	 * 
	 * @param fields segments fields to be filtered. field[0] always contains the segment name.
	 * @return the rewritten or filtered segment fields, or null if the segment should be dropped.
	 */
	protected abstract String[] filterSegment(String[] fields);


}
