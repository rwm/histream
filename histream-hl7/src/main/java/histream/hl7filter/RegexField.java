package histream.hl7filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexField implements SegmentFilter{
	private String segmentId;
	private int field;
	private Pattern matchPattern;
	private String replace;

	
	public RegexField(String segment, int field, String matchPattern, String replace){
		this.segmentId = segment;
		this.field = field;
		this.matchPattern = Pattern.compile(matchPattern);
		this.replace = replace;
	}
	
	int getFieldId(){return field;}

	@Override
	public String getSegmentId() {return segmentId;}

	@Override
	public boolean filter(String[] segment) {
		if( segment.length <= field )
			return false; // matching field not available (=empty) in segment
		Matcher matcher = matchPattern.matcher(segment[field]);
		String result = matcher.replaceAll(replace);
		if( result.equals(segment[field]) == false )
			return false; // nothing replaced
		segment[field] = result;
		return true;
	}
}
