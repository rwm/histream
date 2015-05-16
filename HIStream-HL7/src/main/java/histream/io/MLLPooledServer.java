package histream.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;


/*
		final Thread hook = new Thread(){
			@Override
			public void run(){
				shutdown();
			}
		};
		
		Runtime.getRuntime().addShutdownHook(hook);
		try{
			Runtime.getRuntime().removeShutdownHook(hook);
		}catch( IllegalStateException e ){}
 */
@Deprecated
public class MLLPooledServer implements Closeable, ThreadFactory, RejectedExecutionHandler/*, AdministrableComponent */{
	private static Logger log = Logger.getLogger(MLLPooledServer.class.getName());

	public static final int DEFAULT_BIND_PORT = 5012;
	public static final int DEFAULT_ADMIN_PORT = 8080;

	private ThreadGroup threadGroup;
	private ThreadGroup listenerThreads;
	private LinkedList<ListenerThread> listeners;
	private ExecutorService executor;
	private MLLPacketHandler messageHandler;
	private Date startTime;
	private String adminPassword;
	private Object lock;
	
//	private StatusView adminStatus;
	
	private class ListenerThread implements Runnable{
		ServerSocketChannel server;
		AcknowledgeProvider acknowledger;
		int exitCode;
		AtomicLong connectionCount;

		public ListenerThread(int port, AcknowledgeProvider acknowledger) throws IOException{
			this.server = ServerSocketChannel.open();
			server.configureBlocking(true);
			server.socket().bind(new InetSocketAddress(port));
			this.acknowledger = acknowledger;
			exitCode = 0;
			connectionCount = new AtomicLong();
		}
		@Override
		public void run() {
			while( exitCode == 0 ){
				SocketChannel accepted;
				while( exitCode == 0 ){
					
					try{
						accepted = server.accept();
						connectionCount.incrementAndGet();
					}catch( java.io.IOException e ){
						accepted = null;
						exitCode = 1;
						continue;
					}
					executor.execute(new MLLSocketTask(accepted,messageHandler,acknowledger) );
				}
				
			}
		}
		
	}
	
	
	public MLLPooledServer(int httpAdminPort, MLLPacketHandler handler)throws java.io.IOException{
		this.threadGroup  = new ThreadGroup("MLLThreadPool");
		this.listenerThreads  = new ThreadGroup("MLLListeners");

		this.lock = new Object();
		
		this.executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),this,this );

		this.messageHandler = handler;
		this.listeners = new LinkedList<MLLPooledServer.ListenerThread>();
		
	//	this.adminStatus = new StatusView();
	
	}
	
	/**
	 * Get the time when the server was started.
	 * @return start time.
	 */
	public Date getStartTime(){
		return startTime;
	}
	
	/**
	 * Protect administration views with a password.
	 * @param password administration password.
	 */
	public void setAdminPassword(String password){
		this.adminPassword = password;
	}
	/**
	 * Check whether the provided password argument grants
	 * access to the administration area.
	 * If a password was not previously set via {@link #setAdminPassword(String)},
	 * access is always granted.
	 * @param input user input
	 * @return whether to grant access or not
	 */
	public boolean checkAdminPassword(String input){
		if( this.adminPassword == null )return true;
		else return this.adminPassword.equals(input);
	}

	public void addListener(int port, AcknowledgeProvider acknowledger) throws IOException{
		ListenerThread l = new ListenerThread(port, acknowledger);
		listeners.add(l);
	}
	public void closeListeners(){
		for( ListenerThread l : listeners ){
			try {
				l.server.close();
			} catch (IOException e) {}
		}
	}
	
	

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		log.severe("Connection rejected");
	}

	@Override
	public Thread newThread(Runnable r) {
		MLLPoolThread t = new MLLPoolThread(threadGroup,r);
		if( t.isDaemon() )t.setDaemon(false);
		
		return t;
	}
	
	
	
	/*
	 * Administration views and actions below
	 */
	
	
	/*
	private class StatusView implements AdminView{

		@Override
		public String getId() {
			return "status";
		}

		@Override
		public String getDisplayName() {
			return "Status";
		}

		@Override
		public String getDescription() {
			return "Display server's status";
		}

		@Override
		public String getHtml() {
			DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

			StringBuilder response = new StringBuilder();
			response.append("<p><h2>Status</h2>\n");
			response.append("<table border=\"1\"><tr><th>Property</th><th>Value</th></tr>\n");

			response.append("<tr><td>Current time</td><td>"+df.format(new java.util.Date())+"</td></tr>\n");

			if( executor instanceof ThreadPoolExecutor ){
				ThreadPoolExecutor tpe = (ThreadPoolExecutor)executor;
				response.append("<tr><td>Startup-time</td><td>"+df.format(startTime)+"</td></tr>\n");
				response.append("<tr><td>Active threads</td><td>"+tpe.getActiveCount()+"</td></tr>\n");
				response.append("<tr><td>Working dir</td><td>"+System.getProperty("user.dir")+"</td></tr>\n");
				response.append("<tr><td>Finished tasks</td><td>"+tpe.getCompletedTaskCount()+"</td></tr>\n");
				response.append("<tr><td>Current pool</td><td>"+tpe.getPoolSize()+"</td></tr>\n");
				response.append("<tr><td>Largest pool</td><td>"+tpe.getLargestPoolSize()+"</td></tr>\n");
								
				for( ListenerThread l : listeners ){
					response.append("<tr><th>Listener</th><th>"+l.toString()+"</td></tr>\n");
					response.append("<tr><td>Listen port</td><td>"+l.server.socket().getLocalPort()+"</td></tr>\n");
					response.append("<tr><td>Connections</td><td>"+l.connectionCount.get()+"</td></tr>\n");
					response.append("<tr><td>Acknowledger</td><td>"+l.acknowledger.toString()+"</td></tr>\n");
				}
				
			}
			response.append("</table></p>\n");

			response.append("<p><h2>MLL packet handler</h2>\n");
			response.append("<table border=\"1\"><tr><th>Property</th><th>Value</th></tr>\n");
			response.append("<tr><td>Class-name</td><td>"+messageHandler.getClass().getCanonicalName()+"</td></tr>\n");
			
			Properties props = messageHandler.getStats();
			
			for( String name : props.stringPropertyNames() ){
				response.append("<tr><td>"+name.toString()+"</td><td>"+props.getProperty(name)+"</td></tr>\n");
			}
			response.append("</table></p>\n");
			
			response.append("<p><h2>System</h2>\n");
			response.append("<table border=\"1\"><tr><th>Property</th><th>Value</th></tr>\n");

			props = System.getProperties();
			ArrayList<String> sysprops = new ArrayList<String>(props.stringPropertyNames());
			Collections.sort(sysprops);
			
			for( String name : sysprops ){
				String value = props.getProperty(name);
				if( name.endsWith(".password") )value = "********";
				
				response.append("<tr><td>"+name.toString()+"</td><td>"+value+"</td></tr>\n");
			}
			response.append("</table></p>\n");
			
			return response.toString();
		}

		@Override
		public boolean hasChanged() {return true;}
		
	}


	@Override
	public AdminFunction[] getFunctions() {
		return null;
	}

	@Override
	public AdminView[] getViews() {
		return new AdminView[]{ adminStatus };
	}
*/
	public void open() throws Exception {
		// start listeners
		for( ListenerThread l : listeners ){
			new Thread(listenerThreads, l).start();
		}
		
	}

	@Override
	public void close() throws IOException {
		log.info("Manual shutdown");
		closeListeners();
		//messageHandler.shutdown();
		executor.shutdown();
		try {
			executor.awaitTermination(500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {}
		synchronized( lock ){
			lock.notifyAll();
		}		
	}


	

}
