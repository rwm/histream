package histream.hl7filter;

/**
 * Replace a single field which exactly matches a predefined string.
 * To replace substrings or complicated patterns, use 
 * @author marap1
 *
 */
public class ReplaceField implements SegmentFilter {

	private String segmentId;
	private int field;
	private String search;
	private String replace;
	
	public ReplaceField(String segmentId, int field, String search, String replace){
		this.segmentId = segmentId;
		this.field = field;
		this.search = search;
		this.replace = replace;
	}
	@Override
	public String getSegmentId() {return segmentId;}

	@Override
	public boolean filter(String[] segment) {
		if( segment.length < field )return false;
		if( segment[field].equals(this.search) ){
			segment[field] = this.replace;
			return true;
		}else
			return false;
	}

}
