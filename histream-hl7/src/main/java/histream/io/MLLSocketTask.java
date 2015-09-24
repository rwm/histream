package histream.io;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MLLSocketTask extends MLLChannelReader implements Runnable{
	static Logger log = Logger.getLogger(MLLSocketTask.class.getName());

	private SocketChannel socket;
	private InetAddress remoteAddr;
	private MLLPacketHandler handler;
	private MLLErrorHandler errorHandler;
	private AcknowledgeProvider ack;
	private boolean stopProcessing;
	
	
	public MLLSocketTask(SocketChannel acceptedSocket, MLLPacketHandler handler, AcknowledgeProvider ack){
		this.handler = handler;
		this.socket = acceptedSocket;
		this.ack = ack;
		this.remoteAddr = socket.socket().getInetAddress();
	}
	
	/**
	 * Set an error handler to receive errors during message processing. Any {@link Throwable} thrown by 
	 * the {@link MLLPacketHandler} is forwarded to the error handler.
	 * If no error handler is set or the error handler is set to {@code null}, a severe message is logged
	 * throuhg {@link Logger}.
	 * 
	 * @param errorHandler error handler
	 */
	public void setErrorHandler(MLLErrorHandler errorHandler){
		this.errorHandler = errorHandler;
	}
	
	/**
	 * Get the remote address for this connection
	 * @return remote address
	 */
	public InetAddress getRemoteAddress(){return remoteAddr;}
	
	/**
	 * Stop processing packets after finishing the current MLLP packet
	 */
	public void stopProcessing(){
		this.stopProcessing = true;
	}
	
	public void sendMessage(ByteBuffer message){
		ByteBuffer[] buffers = new ByteBuffer[3];
		buffers[0] = ByteBuffer.wrap(new byte[]{0x0B});
		buffers[1] = message;
		buffers[2] = ByteBuffer.wrap(new byte[]{0x1C,0x0D});
		long bytesToSend = message.remaining() + 3;

		long bytesSent = 0;
		try{
			bytesSent = socket.write(buffers);
		}catch( java.io.IOException e ){
			log.log(Level.SEVERE, "Error while writing to socket", e);
		}
		if( bytesSent != bytesToSend ){
			log.severe("Message transmitted partially: "+bytesSent+" of "+bytesToSend+" sent.");
		}
	}
	
	
	
	@Override
	public void run() {
		assert Thread.currentThread() instanceof PersistentByteBuffer;
		PersistentByteBuffer buffer = (PersistentByteBuffer)Thread.currentThread();
		try {
			super.readChannel(socket, buffer);
		} catch (IOException e) {
			log.log(Level.SEVERE, "IOException during channel read", e);
		}
		try{ 
			socket.close(); 
		} catch (IOException e){
			log.log(Level.WARNING,"Exeption during socket close",e);
		}
	}


	@Override
	public ProcessedMessage processMessage(ByteBuffer buffer) {
		ByteBuffer response = null;
		ProcessedMessage message = null;

		message = handler.processMessage(buffer);
		
		// send ack
		response = ack.generateAcknowledge(message); 
	
		if( response != null )sendMessage(response);
		
		if( stopProcessing ){
			super.stopChannelReads();
		}

		return message;
	}


	@Override
	protected void processingError(ByteBuffer buffer, Throwable error) {
		if( errorHandler != null ){
			errorHandler.processingError(buffer, error);
		}else{
			// catch anything else
			log.log(Level.SEVERE, "Unhandled error during message processing (Thread "+Thread.currentThread().getName()+")", error);
		}
		ByteBuffer response = ack.generateParserErrorResponse(new MLLPacketException(buffer, error));
		if( response != null )
			sendMessage(response);
		else log.warning("No error response available for previous error. Unable to send response.");
	}


	
}
