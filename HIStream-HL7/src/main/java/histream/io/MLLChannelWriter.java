package histream.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This packed handler writes all messages passed to {@link #processMessage(ByteBuffer)}
 * to a {@link WritableByteChannel}. Messages are wrapped according to the lower layer protocol
 * with a header byte 0x0B and trailer bytes 0x1C, 0x0D. All writes to the channel
 * are synchronized on the channel object.
 * 
 * @author RWM
 *
 */
public class MLLChannelWriter implements MLLPacketHandler, Closeable {
	static final Logger log = Logger.getLogger(MLLChannelWriter.class.getName());


	protected WritableByteChannel channel;
	private long bytesWritten;
	
	public MLLChannelWriter(WritableByteChannel channel){
		this.channel = channel;
		bytesWritten = 0;
	}
	

	/**
	 * Writes a message to the channel. The message buffer is wrapped according to the 
	 * lower layer protocol in a header byte 0x0B and trailer bytes 0x1C, 0x0D. 
	 * All writes to the channel are synchronized on the channel object.
	 * @param message message buffer to write to the channel
	 * @throws IOException passed from {@link WritableByteChannel#write(ByteBuffer))}
	 */
	public void writeMessage(ByteBuffer message)throws IOException
	{
		ByteBuffer[] buffers = new ByteBuffer[3];
		buffers[0] = ByteBuffer.wrap(new byte[]{0x0B});
		buffers[1] = message.asReadOnlyBuffer();
		buffers[2] = ByteBuffer.wrap(new byte[]{0x1C,0x0D});
		
		synchronized( channel ){
			/* synchronize to make sure only complete blocks are written */
			for( int i=0; i<buffers.length; i++ )
				channel.write(buffers[i]);

			bytesWritten += message.remaining()+3;
		}
	}
	
	@Override
	public ProcessedMessage processMessage(ByteBuffer message)
	{
		MinimallyProcessedMessage result = new MinimallyProcessedMessage(message);
		try{
			writeMessage(message);
		}catch( java.io.IOException e ){
			result.addProcessingError(new MessageProcessingError("Unable to write to channel",e));
		}
		return result;
	}

	@Override
	public void close() {
		try {
			// no need to synchronize. any write operations will throw exceptions after the close.
			channel.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Close operation on channel failed", e);
		}
	}

	
	public Properties getStats() {
		Properties props = new Properties();
		synchronized( channel ){
			props.setProperty("Bytes written", Long.toString(bytesWritten));
		}
		return props;
	}
	
}
