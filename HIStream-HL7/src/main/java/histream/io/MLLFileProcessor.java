package histream.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.zip.GZIPInputStream;

public class MLLFileProcessor extends MLLChannelReader {

	protected MLLPacketHandler packetHandler;
	private long maxMessages;
	private long numMessages;

	
	public MLLFileProcessor(){
		packetHandler = null;
	}
	
	public MLLFileProcessor(MLLPacketHandler handler){
		this.setPacketHandler(handler);
	}
	
	public void setPacketHandler(MLLPacketHandler handler){
		this.packetHandler = handler;
	}
	
	public void setMaxMessages(int maxMessages){
		this.maxMessages = maxMessages;
	}

	/**
	 * Return the number of processed messages
	 * @return number of processed messages
	 */
	public long getProcessedMessageCount(){return numMessages;}
	
	/*
	public MLLPacketHandler getPacketHandler(){return packetHandler;}
	*/

	@Override
	public final ProcessedMessage processMessage(ByteBuffer buffer) {
		ProcessedMessage ret=null;
		numMessages ++;
		
		ret = packetHandler.processMessage(buffer);

		// TODO: wait if limitBytesPerSecond to fast
		
		if( maxMessages != 0 && numMessages >= maxMessages )stopChannelReads();
		return ret;
	}
	
	/**
	 * Convenience method to read a file
	 * @param filename
	 * @return number of bytes read
	 * @throws java.io.IOException
	 */
	public final long readFile(String filename)throws java.io.IOException{
		return readFile(new File(filename), false);
	}
	
	/**
	 * Convenience method to read a gzipped file
	 * @param filename
	 * @return number of bytes read
	 * @throws java.io.IOException
	 */
	public final long readGZippedFile(String filename)throws java.io.IOException{
		return readFile(new File(filename), true);
	}
	
	
	/**
	 * Convenience method to read a file
	 * @param file
	 * @return number of bytes read
	 * @throws java.io.IOException
	 */
	public final long readFile(File file, boolean gzip)throws java.io.IOException{
		InputStream in = new FileInputStream(file);
		if( gzip == true ){
			in = new GZIPInputStream(in); 
		}		
		long bytesRead = readChannel(Channels.newChannel(in));
		in.close();
		return bytesRead;
	}

	@Override
	protected void processingError(ByteBuffer buffer, Throwable error) {
		System.err.println("Unhandled error during message processing: "+error);
		error.printStackTrace();
	}

}
