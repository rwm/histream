package histream.hl7.filter;


import histream.hl7.Message;
import histream.hl7.MessageHandler;
import histream.io.ProcessedMessage;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * Message handler which wraps all messages in a minimal lower layer frame
 * and writes the frame to a byte channel.
 * The ByteChannel can be set with {@link #setOutputChannel(WritableByteChannel)}
 * 
 * Encoding is done using the system property histream.hl7.charset or the default 
 * encoding ISO-8859-1.
 * TODO: Use encoding specified in message.
 * 
 * @author marap1
 *
 */
public class MLLChannelForwarder implements MessageHandler {

	private WritableByteChannel out;
	private CharBuffer charBuffer;
	private ByteBuffer byteBuffer;
	private CharsetEncoder encoder;
	private ByteBuffer headerBytes;
	private ByteBuffer trailerBytes;
	
	public MLLChannelForwarder(){
		this.out = null;
		charBuffer = CharBuffer.allocate(1024*1024*2);
		byteBuffer = ByteBuffer.allocate(1024*1024*2);
		// TODO: support different encodings specified in the MSH segment
		Charset charset = Charset.forName(System.getProperty("histream.hl7.charset", "ISO-8859-1"));
		encoder = charset.newEncoder();
		encoder.onMalformedInput(CodingErrorAction.REPLACE);
	    encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
	    headerBytes = ByteBuffer.wrap(new byte[]{0x0B});
		trailerBytes = ByteBuffer.wrap(new byte[]{0x1C,0x0D});

	}
	
	
	public void setOutputChannel(WritableByteChannel out){
		this.out = out;
	}
	public WritableByteChannel getOutputChannel(){return this.out;}


	/**
	 * Writes the message to the associated output channel surrounded by 
	 * minimum lower layer protocol bytes.
	 */
	@Override
	public ProcessedMessage processMessage(Message message) {
		charBuffer.clear();
		byteBuffer.clear();
		encoder.reset();
		
		try{
			message.write(charBuffer);
			charBuffer.flip();
			CoderResult cr = encoder.encode(charBuffer, byteBuffer, true);
			if( cr.isOverflow() )throw new IOException(new BufferOverflowException());
			cr = encoder.flush(byteBuffer);
			if( cr.isOverflow() )throw new IOException(new BufferOverflowException());
			
		}catch( IOException e ){
			// any IOException while writing to a CharBuffer should be caused by insufficient buffer space
			if( e.getCause() == null || !(e.getCause() instanceof BufferOverflowException) )
				throw new RuntimeException("Unable to write message buffers", e);
			
			// double buffer sizes and retry
			charBuffer = CharBuffer.allocate(charBuffer.capacity()*2);
			byteBuffer = ByteBuffer.allocate(byteBuffer.capacity()*2);
			// recursion
			processMessage(message);
		}
		
		byteBuffer.flip();
		headerBytes.clear();
		trailerBytes.clear();
		
		try {
			out.write(headerBytes);
			out.write(byteBuffer);
			out.write(trailerBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// TODO: return failure in case of IOException
		return message;
	}

}
