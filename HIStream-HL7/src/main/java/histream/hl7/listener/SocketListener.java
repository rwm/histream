package histream.hl7.listener;

import histream.io.AcknowledgeProvider;
import histream.io.HL7Parser;
import histream.io.MLLFileMessageHandler;
import histream.io.MLLPacketHandler;
import histream.io.MLLPoolThread;
import histream.io.MLLSocketTask;
import histream.io.ProcessedMessage;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Accept connections from a server socket and let {@link SocketTask} 
 * handle connections with an {@link ExecutorService}.
 * 
 * The method {@link #open()} must be called before executing the {@link #run()}
 * method. While running, the listener can be canceled with {@link #close()} which
 * can block for a short time until {@link #run()} is exited.
 * 
 * @author marap1
 *
 */
public class SocketListener implements Runnable, Closeable, MLLPacketHandler, RejectedExecutionHandler, ThreadFactory{
	private static Logger log = Logger.getLogger(SocketListener.class.getName());
	private ServerSocketChannel server;
	private AcknowledgeProvider acknowledger;
	private ExecutorService executor;
	private ThreadGroup threadGroup;
	private int port;
	private Status status;
	private AtomicLong connectionCount;
	private HL7Parser parser;
	private MLLFileMessageHandler dumpFile;
	private Object lock;
	//private static final int CLOSE_WAIT_TIMEOUT = 2000;


	private enum Status{
		INITIALIZED,
		LISTENING,
		EXITING,
		CLOSED
	}
	public SocketListener(int port, AcknowledgeProvider acknowledger) throws IOException{
		this(port, acknowledger, null);
		this.executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),this,this);
		
	}
	
	public SocketListener(int port, AcknowledgeProvider acknowledger, ExecutorService executor) throws IOException{
		this.acknowledger = acknowledger;
		this.port = port;
		status = Status.INITIALIZED;
		this.lock = new Object();
		this.dumpFile = null;
		this.executor = executor;
		this.threadGroup = new ThreadGroup("SocketListener[port="+port+"]");
	}
	
	public void setParser(HL7Parser parser){
		this.parser = parser;
	}
	
	/**
	 * Sets a dump handler for dumping HL7 messages to a file. The handler must be 
	 * opened and closed manually and is not touched by the {@link #open()} 
	 * and {@link #close()} methods.
	 *  
	 * @param dumpFile
	 */
	public void setDumpFile(MLLFileMessageHandler dumpFile){
		this.dumpFile = dumpFile;
	}
	
	@Override
	public void run() {
		log.fine("Listening on port "+port);
		synchronized( lock ){
			status = Status.LISTENING ;
		}

		SocketChannel accepted;
		while( status == Status.LISTENING ){
			
			try{
				accepted = server.accept();
				connectionCount.incrementAndGet();
			}catch( java.io.IOException e ){
				accepted = null;
				status = Status.EXITING;
				continue;
			}
			executor.execute(new MLLSocketTask(accepted,this,acknowledger) );
		}
			
		synchronized( lock ){
			this.status = Status.CLOSED;
			lock.notifyAll();
		}
		log.fine("Stopped listening on port "+port);
	}
	
	@Override
	public void close(){
		synchronized( lock ){
			if( status != Status.LISTENING ){
				log.warning("Trying to close while not listening");
				return;
			}
			
			status = Status.EXITING;
			try{
				server.close();
			} catch (IOException e) {
				log.log(Level.WARNING,"Error during manual socket shutdown.",e);
			}
			// wait for runnable to exit
			try{
				lock.wait();
			}catch( InterruptedException e ){
			}
		}
		if( status != Status.CLOSED ){
			log.warning("Probably failed to close listener.");
		}
	}

	public void open() throws IOException {
		if( status != Status.INITIALIZED ){
			log.warning("Opening listener in status "+status+". Expecting state "+Status.INITIALIZED);
		}
		this.server = ServerSocketChannel.open();
		server.configureBlocking(true);
		server.socket().bind(new InetSocketAddress(port));
		connectionCount = new AtomicLong();
		// dump file is already open
	}

	
	@Override
	public ProcessedMessage processMessage(ByteBuffer message) {
		// dump to file
		if( dumpFile != null ){
			dumpFile.processMessage(message);
		}
		ProcessedMessage result = parser.processMessage(message);
		return result;
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		log.warning("Unable to handle connection request due to overload.");
	}

	@Override
	public Thread newThread(Runnable r) {
		// TODO: is there a way to hide pool thread and sockettask?
		// TODO: clean up pool thrad
		MLLPoolThread t = new MLLPoolThread(threadGroup,r);
		if( t.isDaemon() )t.setDaemon(false);
		
		return t;
	}

	
}
