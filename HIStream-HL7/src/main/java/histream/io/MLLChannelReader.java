/* Copyright (c) 2012, Raphael W. Majeed
 * All rights reserved. 
 * This file is subject to the conditions of the Simplified BSD License.
 * See LICENSE.txt or http://sourceforge.net/projects/histream/files/LICENSE.txt/view
 */
package histream.io;

import histream.hl7.Message;
import histream.hl7.filter.FilteringDecoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MLLChannelReader implements MLLPacketHandler{

	static final Logger log = Logger.getLogger(MLLChannelReader.class.getName());
	
	@Override
	public abstract ProcessedMessage processMessage(ByteBuffer buffer);
	
	/**
	 * Override this method to process errors thrown during {@link #processMessage(ByteBuffer)}.
	 * This method is guaranteed to be called immediately after a failed processMessage and always before
	 * a possibly next call to processMessage.
	 * 
	 * @param buffer data which caused the failure 
	 * @param error thrown error
	 */
	protected abstract void processingError(ByteBuffer buffer, Throwable error);
	
	/**
	 * 
	 */
	private int exitCode;
	
	/**
	 * Causes all calls to {@link #readChannel(ReadableByteChannel)} to stop processing. Pending
	 * {@link ReadableByteChannel#read(ByteBuffer)} operations are not interrupted. To stop all reads 
	 * immediately, the channels need to be closed manually.
	 */
	public void stopChannelReads(){
		this.exitCode = 2; // manual exit
		
	}
	/**
	 * Reads lower layer messages from a byte channel until the channel is closed or end of channel is encountered.
	 * The maximum number of available bytes is read from the channel and as many as possible lower layer packets are
	 * processed (e.g. stripped of 0x0B - 0x1C 0x0D wrapper and passed to {@link #processMessage(ByteBuffer)}.
	 * As long as provided channel and byte buffer is not reused, the method might be called by multiple threads at once. In
	 * this case, the implementation of {@link #processMessage(ByteBuffer)} needs to handle thread synchronization.
	 * @param channel Byte channel to read from
	 * @param pb byte buffer to user for reading
	 * @return number of bytes read
	 * @throws IOException Exception thrown while reading from the channel, or if the protocol MLLP frame is errorneous or if the message does not fit into the buffer 
	 */
	public final long readChannel(ReadableByteChannel channel, PersistentByteBuffer pb) throws IOException{		
		pb.initBuffer();
		
		ByteBuffer buffer = pb.getBuffer();
		buffer.clear();
		
		int result,pos=0,start=0;

		java.io.IOException exception = null;
		boolean mllpLayer = true;
		this.exitCode = 0;
		long bytesRead = 0;
		//long messageBytes = 0;
		
		
		while( exitCode == 0 ){
			/* can throw IOException */
			result = channel.read(buffer);

			if( result == -1 ){
				/* end of stream */
				exitCode = 1;
				continue;
			}else
				bytesRead += result;
			
			buffer.flip();
			
			// messages always need to begin with mllp header
			mllpLayer = true;
			
			while( exitCode == 0 && mllpLayer && buffer.hasRemaining() ){
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
						ByteBuffer copy = buffer.asReadOnlyBuffer();
						copy.position(start).limit(pos);
						//messageBytes += copy.remaining()+3;
						
						try{
							processMessage(copy);
						}catch( Throwable e ){
							// restore copy
							copy = buffer.asReadOnlyBuffer();
							copy.position(start).limit(pos);
							// process error
							processingError(copy, e);
						}

						// advance buffer position (allows processed space to be overwritten)
						buffer.position(pos+2);
						
						mllpLayer = true;
						break;
					}else pos ++;
				}
				
				if( mllpLayer == false // end of message not found
						&& buffer.position() == 0 // compacting will not give more space
						&& buffer.limit()==buffer.capacity() ) // buffer is at full capacity
				{
					int cap = buffer.capacity();
					buffer = pb.enlargeBuffer();
					if( buffer == null ){
						log.severe("Unable to enlarge buffer");
						throw new java.io.IOException("Unable to allocate buffer: MessageFilter exceeds maximum allowed length (>"+(pos+1)+", cap="+cap+")!");
					}else{
						log.info("Enlarged message buffer to "+buffer.capacity()+" bytes");
					}
					// buffer was enlarged
					// since mllpLayer is false, parsing loop is left to read more data
				}
				
			}// end while ( mllpLayer && buffer.hasRemaining() )
			buffer.compact();

		}
		//log.info("Read "+bytesRead+" bytes, processed "+messageBytes+" bytes.");
		return bytesRead;
	}

	/**
	 * TODO: delete
	 * @param channel
	 * @param pb
	 * @return number of bytes read
	 * @throws IOException Exception thrown while reading from the channel, or if the protocol MLLP frame is errorneous or if the message does not fit into the buffer 
	 */
	@Deprecated
	public final long readChannelOriginal(ReadableByteChannel channel, PersistentByteBuffer pb) throws IOException{		
		pb.initBuffer();
		
		ByteBuffer buffer = pb.getBuffer();
		buffer.clear();
		
		int result,pos=0;
		
		java.io.IOException exception = null;
		boolean mllpLayer = true;
		this.exitCode = 0;
		long bytesRead = 0;
		long messageBytes = 0;
		
		int count=0;
		
		while( exitCode == 0 ){
			/* can throw IOException */
			result = channel.read(buffer);

			if( result == -1 ){
				/* end of stream */
				exitCode = 1;
				continue;
			}else
				bytesRead += result;
			
			if( mllpLayer ){
				if( buffer.get(0) != 0x0B ){
					log.log(Level.SEVERE, "Unexpected byte in MLLP decoding layer", exception);

					throw new java.io.IOException("Unexpecteded byte 0x"+Integer.toHexString(buffer.get(0))+" read. LLP-Header 0x0B expected.");
				}
				mllpLayer = false;				
				pos = 1;
				
			}
			if( mllpLayer == false ){
				while( pos+1 < buffer.position() ){
					// Find end of message
					if( buffer.get(pos)==0x1C && buffer.get(pos+1)==0x0D ){
						ByteBuffer copy = buffer.asReadOnlyBuffer();
						copy.position(1).limit(pos);
						messageBytes += copy.remaining()+3;

						// debug:
						//
						count ++;
						if( count >= 45567 ){
							// TODO 
							Message m = FilteringDecoder._decode(copy.asReadOnlyBuffer());
							System.out.println("Stop");
							if( m == null )m=null;
						}
						// end debug
						
						try{
							processMessage(copy);
						}catch( Throwable e ){
							// restore copy
							copy = buffer.asReadOnlyBuffer();
							copy.position(1).limit(pos);
							// process error
							processingError(copy, e);
						}
						buffer.flip();
						buffer.position(pos+2);
						buffer.compact();
						mllpLayer = true;
						pos = 1;
						break;
					}else pos ++;
				}
				
				if( pos+1 == buffer.limit() /*&& buffer.limit() == buffer.capacity()*/ ){
					int cap = buffer.capacity();
					buffer = pb.enlargeBuffer();
					if( buffer == null ){
						log.log(Level.SEVERE, "Unable to enlarge buffer", exception);
						throw new java.io.IOException("Unable to allocate buffer: MessageFilter exceeds maximum allowed length (>"+(pos+1)+", cap="+cap+")!");
					}
				}
			}
		}
		
		log.info("Read "+bytesRead+" bytes, processed "+messageBytes+" bytes.");
		return bytesRead;
	}

	/**
	 * Convenience method which allocates needed memory
	 * @see MLLChannelReader#readChannel(ReadableByteChannel, PersistentByteBuffer)
	 */
	public final long readChannel(ReadableByteChannel channel) throws IOException{
		return readChannel(channel, new MLLPoolThread(null, null));
	}
	
}
