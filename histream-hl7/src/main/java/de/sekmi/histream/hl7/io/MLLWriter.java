package de.sekmi.histream.hl7.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;

public class MLLWriter implements Consumer<ByteBuffer>{

	protected WritableByteChannel channel;
	
	public MLLWriter(WritableByteChannel channel){
		this.channel = channel;
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

			//bytesWritten += message.remaining()+3;
		}
	}

	@Override
	public void accept(ByteBuffer t) {
		try {
			writeMessage(t);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
