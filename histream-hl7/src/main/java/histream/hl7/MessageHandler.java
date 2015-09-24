package histream.hl7;

import histream.io.ProcessedMessage;

/**
 * Process parsed HL7 messages. The method {@link #processMessage(Message)} can be
 * called by multiple threads concurrently. Thus, synchronization should be implemented
 * if necessary.
 *  
 * @author RWM
 *
 */
public interface MessageHandler {
	ProcessedMessage processMessage(Message message);
}
