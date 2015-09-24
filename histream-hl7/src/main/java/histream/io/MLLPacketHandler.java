package histream.io;

import java.nio.ByteBuffer;

/**
 * Interface to handle raw minimal lower layer packets.
 * The method {@link #processMessage(ByteBuffer)} can be accessed by multiple threads. 
 * Startup and shutdown methods must be called prior resp. after processing.
 *  
 * @author RWM
 * 
 */
public interface MLLPacketHandler {
	
	ProcessedMessage processMessage(ByteBuffer message);
	
	//void startup() throws java.io.IOException;
	//void shutdown();
	
	//Properties getStats();
}
