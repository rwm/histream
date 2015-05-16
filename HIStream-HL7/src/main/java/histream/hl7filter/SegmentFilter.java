package histream.hl7filter;

/**
 * Filter interface for specific segments.
 * 
 * @author marap1
 *
 */
public interface SegmentFilter {
	/**
	 * Get the segment id which can be filtered.
	 * @return segment id
	 */
	String getSegmentId();
	
	/**
	 * Filter the supplied segment.
	 * @param segment segment to filter. <code>segment[0]</code> contains the segment id.
	 * @return whether the filter was applied successfully. Return false if nothing changed.
	 */
	boolean filter(String[] segment);
}
