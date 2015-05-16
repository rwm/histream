package histream.hl7;

import histream.io.AbstractProcessedMessage;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


public class Message extends AbstractProcessedMessage{
	static final Logger log = Logger.getLogger(Message.class.getName());
	protected List<String[]> segments;
	protected char[] encodingChars;
	
	public static final char segmentTerminatorChar = '\r';

	
	public Message(List<String[]> segments){
		this.segments = segments;
		loadEncodingChars();
	}
	
	protected void loadEncodingChars(){
		if( segments == null || segments.size() == 0 )return;
		
		this.encodingChars = segments.get(0)[2].toCharArray();
	}
	
	/**
	 * Splits a string into substrings. The provided
	 * separation character is used.
	 * 
	 * @param str String to split
	 * @param sep Separation character
	 * @return Array containing the split strings.
	 */
	public static String[] split(String str, char sep){
		ArrayList<String> comps = new ArrayList<String>();
		int p=0,i;
		while( (i = str.indexOf(sep,p)) != -1 ){
			comps.add(str.substring(p, i));
			p=i+1;
		}
		if( p < str.length() )comps.add(str.substring(p));
		
		return comps.toArray(new String[comps.size()]);
	}
	
	public static String join(String[] strs, char glue){
		if( strs.length == 0 )return "";
		else if( strs.length == 1 )return strs[0];

		StringBuilder b = new StringBuilder();
		
		b.append(strs[0]);
		for( int i=1; i<strs.length; i++ ){
			b.append(glue);
			b.append(strs[i]);
		}
		return b.toString();
	}

	public String toHL7v2String(){
		if( segments.size() == 0 )return "";
		StringBuilder b = new StringBuilder();

		String[] msh = segments.get(0);
		char fs = msh[1].charAt(0);
		assert msh[0].equals("MSH") : "first segment must be MSH";
		
		// write MSH
		b.append(msh[0]);
		for( int i=2; i<msh.length; i++ ){
			b.append(fs);
			b.append(msh[i]);
		}
		b.append(segmentTerminatorChar);
		
		// write remaining segments */
		for( int i=1; i<segments.size(); i++ ){
			String[] fields = segments.get(i);
			b.append(fields[0]);
			for( int j=1; j<fields.length; j++ ){
				b.append(fs);
				b.append(fields[j]);
			}
			b.append(segmentTerminatorChar);
		}
		return b.toString();
	}
	
	@Override
	public String toString(){
		if( segments.size() == 0 )return "[]";
		else return toHL7v2String();
	}
	
	public List<String[]> getSegments(){
		return segments;
	}
	
	public String[] getSegment(int index){
		return segments.get(index);
	}
	
	public int numSegments(){
		return segments.size();
	}
	
	public String getSegmentId(int index){
		return getSegment(index)[0];
	}
	
	/**
	 * Splits a field into its components. The correct
	 * component separator character from the MSH segment
	 * is used for separation (e.g ^).
	 * 
	 * @param field
	 * @return Array containing all components
	 */
	public String[] splitFieldComponents(String fields){
		return split(fields, encodingChars[0]);
	}
	/**
	 * Splits a field into its subcomponents. The correct
	 * subcomponent separator character from the MSH segment
	 * is used for separation (e.g. &).
	 * 
	 * @param field
	 * @return Array containing all subcomponents
	 */
	public String[] splitSubComponents(String field){
		return split(field,encodingChars[3]);
	}
	
	public String joinFieldComponents(String[] fields){
		return join(fields, encodingChars[0]);
		
	}
	public String joinSubComponents(String[] subcomps){
		return join(subcomps, encodingChars[3]);
	}

	/**
	 * Write the message to the given appendable (eg. a {@link CharBuffer} or System.out).
	 * TODO: write unit test to verify that IOException is caused by {@link BufferOverflowException} for CharBuffers 
	 * @param out
	 * @throws IOException if there is an output error or not enough space in the output buffer in case of CharBuffer
	 */
	public void write(Appendable out)throws IOException{
		// write message
		if( segments.size() == 0 ){
			// message cleared/dropped by filter
			// return empty buffer
		}else{
			// write processed segments
			
			// reload field separator, in case of change
			String[] msh = segments.get(0);
			char fs = msh[1].charAt(0);
			
			// write msh separately (b/c special handling of separator characters)
			out.append(msh[0]);
			out.append(msh[1]);
			out.append(msh[2]);
			for( int i=3; i<msh.length; i++ ){
				out.append(fs);
				out.append(msh[i]);				
			}			
			out.append(segmentTerminatorChar);
			// TODO: find way to use specified encoding and write directly to byte buffer
			
			// write remaining segments
			for( int i=1; i<segments.size(); i++ ){
				String[] s = segments.get(i);
				
				out.append(s[0]);
				for( int j=1; j<s.length; j++ ){
					out.append(fs);
					out.append(s[j]);
				}
				out.append(segmentTerminatorChar);
			}
		}
		
	}

	@Override
	public String getMessageID() {
		if( segments.size() == 0 || segments.get(0).length <= 10 )return null;
		else return segments.get(0)[10];
	}
	
	public static Date dateFromHL7(String hl7date){
		Calendar cal = Calendar.getInstance();
		cal.clear();
		
		if( hl7date.length() >= 4 ){
			cal.set(Calendar.YEAR, Integer.parseInt(hl7date.substring(0, 4)));
		}else{
			log.severe("HL7 date without year segment");
		}
		if( hl7date.length() >= 6 ){
			// important: Calendar.MONTH starts with 0 (=Januar)
			// therefore, the parsed value is decremented by 1
			cal.set(Calendar.MONTH, Integer.parseInt(hl7date.substring(4,6)) - 1);
		}
		if( hl7date.length() >= 8 ){
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(hl7date.substring(6,8)));
		}
		if( hl7date.length() >= 10 ){
			cal.set(Calendar.HOUR, Integer.parseInt(hl7date.substring(8,10)));
		}
		if( hl7date.length() >= 12 ){
			cal.set(Calendar.MINUTE, Integer.parseInt(hl7date.substring(10,12)));
		}
		if( hl7date.length() >= 14 ){
			cal.set(Calendar.SECOND, Integer.parseInt(hl7date.substring(12,14)));
		}
		return cal.getTime();
	}
	
	public Date getMessageTime(){
		if( segments.size() == 0 || segments.get(0).length <= 7 )return null;
		else return dateFromHL7(segments.get(0)[7]);
	}
	
	/**
	 * Compact the message by removing trailing empty fields for every segment.
	 * TODO: test/verify whether the last field is kept.
	 */
	public void compact(){
		for( int i=0; i<segments.size(); i++ ){
			String[] seg = segments.get(i);
			
			if( seg[seg.length-1].length() == 0 ){
				int lastUsedField = seg.length-1;
				while( lastUsedField > 0 && seg[lastUsedField-1].length() == 0 )
					lastUsedField --;
				
				segments.set(i, Arrays.copyOf(seg, lastUsedField));
			}
		}
		throw new UnsupportedOperationException();
	}
	
	public void dump(){
		try {
			write(System.out);
		} catch (IOException e) {
			System.err.println("Error while dumping message: "+e.getMessage());
			e.printStackTrace();
		}
	}
}
