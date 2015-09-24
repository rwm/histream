package histream.io;

import java.util.Collection;


public interface ProcessedMessage {
	//ByteBuffer getRawBytes();
	String getMessageID();
	
	boolean hasProcessingErrors();
	// TODO: collect processing errors as suppressed exceptions in e.g. MLLPacketException
	Collection<MessageProcessingError> getProcessingErrors();
	
}
