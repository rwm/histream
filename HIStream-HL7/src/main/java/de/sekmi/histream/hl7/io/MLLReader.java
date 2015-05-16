package de.sekmi.histream.hl7.io;

import histream.io.PersistentByteBuffer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * Reads lower layer messages from a byte channel until the channel is closed or end of channel is encountered.
 * The maximum number of available bytes is read from the channel and as many as possible lower layer packets are
 * processed (e.g. stripped of 0x0B - 0x1C 0x0D wrapper and passed to {@link #processMessage(ByteBuffer)}.
 * As long as provided channel and byte buffer is not reused, the method might be called by multiple threads at once. In
 * this case, the implementation needs to handle thread synchronization.

 * @author Raphael
 *
 */
public class MLLReader implements Supplier<ByteBuffer> {
	private ReadableByteChannel channel;
	private PersistentByteBuffer pb;
	private Queue<ByteBuffer> fifo;
	
	public MLLReader(ReadableByteChannel channel, PersistentByteBuffer buffer){
		this.channel = channel;
		this.pb = buffer;
		this.fifo = new LinkedList<>();
	}
	/**
	 * @param channel Byte channel to read from
	 * @param pb byte buffer to user for reading
	 * @return number of bytes read
	 * @throws IOException Exception thrown while reading from the channel, or if the protocol MLLP frame is errorneous or if the message does not fit into the buffer 
	 */
	public ByteBuffer getOrException()throws IOException{		
		pb.initBuffer();
		
		ByteBuffer buffer = pb.getBuffer();
		buffer.clear();
		ByteBuffer copy = null;
		int result,pos=0,start=0;

		java.io.IOException exception = null;
		boolean mllpLayer = true;
		
		
		while( fifo.isEmpty() ){
			// read channel to fill queue
			/* can throw IOException */
			result = channel.read(buffer);

			if( result == -1 ){
				/* end of stream */
				return null;
			}
			
			buffer.flip();
			
			// messages always need to begin with mllp header
			mllpLayer = true;
			
			while( mllpLayer && buffer.hasRemaining() ){
				pos = buffer.position();
				
				// first byte should be the lower layer header byte
				if( buffer.get(pos) != 0x0B ){
					exception = new java.io.IOException("Unexpecteded byte 0x"+Integer.toHexString(buffer.get(pos))+" read. LLP-Header 0x0B expected.");
					//log.log(Level.SEVERE, "Unexpected byte in MLLP decoding layer", exception);
					throw exception;
				}
				mllpLayer = false;
				// position after header byte
				pos += 1;
				start = pos;
				
				
				while( pos+1 < buffer.limit() ){
					// find end of message
					if( buffer.get(pos)==0x1C && buffer.get(pos+1)==0x0D ){
						// end of message found at pos
						copy = buffer.asReadOnlyBuffer();
						copy.position(start).limit(pos);
						//messageBytes += copy.remaining()+3;
						fifo.add(copy);
						
						// advance buffer position (allows processed space to be overwritten)
						buffer.position(pos+2);
						
						mllpLayer = true;

						break;

					}else pos += 2;
				}
				
				if( mllpLayer == false // end of message not found
						&& buffer.position() == 0 // compacting will not give more space
						&& buffer.limit()==buffer.capacity() ) // buffer is at full capacity
				{
					int cap = buffer.capacity();
					buffer = pb.enlargeBuffer();
					if( buffer == null ){
						//log.severe("Unable to enlarge buffer");
						throw new java.io.IOException("Unable to allocate buffer: MessageFilter exceeds maximum allowed length (>"+(pos+1)+", cap="+cap+")!");
					}else{
						//log.info("Enlarged message buffer to "+buffer.capacity()+" bytes");
					}
					// buffer was enlarged
					// since mllpLayer is false, parsing loop is left to read more data
				}
				
			}// end while ( mllpLayer && buffer.hasRemaining() )
			buffer.compact();

		}
		
		//log.info("Read "+bytesRead+" bytes, processed "+messageBytes+" bytes.");
		return fifo.remove();
	}
	@Override
	public ByteBuffer get() {
		try {
			return getOrException();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
