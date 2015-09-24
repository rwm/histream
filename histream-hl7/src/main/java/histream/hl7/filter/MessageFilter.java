package histream.hl7.filter;

import histream.hl7.Message;

import java.util.List;

/**
 * A filter interface to process/rewrite some or all segments of 
 * a HL7 2.x message.
 * 
 * TODO: add functionality to prepend or append new segments.
 * 
 * @author marap1
 *
 */
public interface MessageFilter {
	
	/**
	 * Filter the whole message. Segments can be dropped and inserted.
	 * If encoding characters are changed, all remaining segments should
	 * be changed accordingly.
	 * @param segments list of segments split by fields. 
	 */
	void filter(Message message);
	
	/**
	 * Preview the MSH segment of the next message. The return value determines
	 * how the message is processed.
	 * <ul>-1 to drop the entire message (it is not passed to {@link #filter(List)}</ul>
	 * <ul>0 to ignore the message (unfiltered, but might be filtered by other filters)</ul>
	 * <ul>1 to filter the message (passed to {@link #filter(List)})</ul>
	 * @param msh MSH segment of the message which is to be processed
	 * @return 0 to ignore the message, 1 to filter the message, -1 to drop the message.
	 */
	int previewMSH(String[] msh);
}
