package histream.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;


/**
 * A packet handler which writes all packets to a file.
 *  
 * @author marap1
 *
 */
public class MLLFileMessageHandler extends MLLChannelWriter {


	AtomicInteger messageCount;
	//private FileOutputStream fileStream;
	File file;
		

	protected MLLFileMessageHandler(WritableByteChannel channel){
		super(channel);
		messageCount = new AtomicInteger(0);		
	}
	/**
	 * Create a new file message handler which appends to the specified file.
	 * The file is created if it does not exist.
	 * 
	 * @param file File which may or may not exist.
	 * @throws java.io.IOException
	 */
	public MLLFileMessageHandler(File file) throws java.io.IOException{
		this(new FileOutputStream(file, true));
		this.file = file;
	}
	
	public MLLFileMessageHandler(FileOutputStream fileStream){
		this(fileStream.getChannel());
		//this.fileStream = fileStream;
		this.file = null;
	}
	
	public MLLFileMessageHandler(String filename)throws java.io.IOException{
		this(new File(filename));
	}
	
	/**
	 * Creates a new file message handler which writes a gzipped file.
	 * The specified file is created. If the file already exists, XXX
	 * @param file Not existing file.
	 * @return a handler writing gzipped data to the file.
	 * @throws IOException passed from the initialization of the gzip stream.
	 */
	public static MLLFileMessageHandler GZippedHandler(File file) throws IOException{
		MLLFileMessageHandler me = new MLLFileMessageHandler(Channels.newChannel(new GZIPOutputStream(new FileOutputStream(file,false))));
		me.file = file;
		return me;
	}
	

	@Override
	public ProcessedMessage processMessage(ByteBuffer message) 
	{
		messageCount.incrementAndGet();
		return super.processMessage(message);
	}


	
	public Properties getStats() {
		Properties props = super.getStats();
		
		props.setProperty("Message count", Integer.toString(messageCount.get()));
		try {
			props.setProperty("File", file.getCanonicalPath());
		} catch (IOException e) {
		}
		return props;
	}


}