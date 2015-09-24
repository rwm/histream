package histream.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Accepts MLL-packets and transfers them over a socket connection.
 * After each transferred packet, a reply (ack/error) is read from the
 * socket.
 * 
 * @author marap1
 *
 */
public class MLLSocketSender implements MLLPacketHandler, Closeable {
	private static Logger log = Logger.getLogger(MLLSocketSender.class.getName());

	private SocketChannel socket;
	private int totalPackets;
	private int maxPacketsPerConnection;
	private int numPacketsInConnection;
	private boolean readResponse;
	
	private SocketAddress address;
	
	private ByteBuffer mllHeader = ByteBuffer.wrap(new byte[]{0x0B});
	private ByteBuffer mllTrailer = ByteBuffer.wrap(new byte[]{0x1C,0x0D});
	
	public MLLSocketSender(SocketAddress address, int maxPacketsPerConnection, boolean readResponse){
		this.address = address;
		this.maxPacketsPerConnection = maxPacketsPerConnection;
		numPacketsInConnection = 0;
		totalPackets = 0;
		this.readResponse = readResponse;
	}
	
	/**
	 * Tries to read acknowledgement/error response from the bi-directional channel
	 * after each message sent.
	 * 
	 * @param message
	 */
	protected void readAfterTransmission(MinimallyProcessedMessage message){
		// TODO: read and process different response types
		// TODO: only read response if configured to do so
		ByteBuffer response = ByteBuffer.allocate(4);
		try {
			socket.read(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public ProcessedMessage processMessage(ByteBuffer message){
		MinimallyProcessedMessage msg = new MinimallyProcessedMessage(message);
		
		if( (maxPacketsPerConnection != 0 && numPacketsInConnection == maxPacketsPerConnection) 
				|| (socket != null && !socket.isConnected()) ){
			try {
				socket.close();
				socket.socket().close();
			} catch (IOException e) {
			}
			socket = null;
			numPacketsInConnection = 0;
		}		
		
		try {
			socket.write(new ByteBuffer[]{
					mllHeader.asReadOnlyBuffer(),
					message.asReadOnlyBuffer(),
					mllTrailer.asReadOnlyBuffer()
			});
		} catch (IOException e) {
			try {
				socket.close();
				socket.socket().close();
				socket = null;
			} catch (IOException e1) {
				log.log(Level.WARNING,"Exception during socket close (after write exception)",e1);
			}
			msg.addProcessingError(new MessageProcessingError("Unable to write to socket", e));
		}

		
		if( readResponse ){
			readAfterTransmission(msg);
			// TODO: validate/process response
		}
		
		totalPackets ++;
		numPacketsInConnection ++;
		
		return msg;
	}

	public void open() throws IOException{
		socket = SocketChannel.open();
		socket.configureBlocking(true);
		socket.connect(address);
	}

	@Override
	public void close() throws IOException{
		socket.close();
		socket.socket().close();
	}

	
	public Properties getStats() {
		Properties props = new Properties();
		props.setProperty("Total packets", Integer.toString(totalPackets));
		props.setProperty("Max packets per connection", Integer.toString(maxPacketsPerConnection));
		
		return props;
	}

}
