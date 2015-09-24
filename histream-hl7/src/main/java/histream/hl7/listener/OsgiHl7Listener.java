package histream.hl7.listener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import histream.io.AcknowledgeProvider;
import histream.io.ConstantAcknowledger;
import histream.io.HL7Parser;
import histream.io.MLLFileMessageHandler;
import histream.io.NoAcknowledgement;
import histream.io.SimpleHL7Acknowledger;


// referenced in component.xml
public class OsgiHl7Listener implements ThreadFactory, RejectedExecutionHandler{
	private HL7Parser parser;
	private SocketListener listener;
	private Thread thread;
	private AcknowledgeProvider acknowledger;
	private ThreadPoolExecutor executor;
	private ThreadGroup poolThreads;
	/** time in milliseconds to wait for processing/pool threads to exit */
	private int shutdownWaitTime;
	private MLLFileMessageHandler dumpFile;
	//private String dumpFilename;
	private static final String DUMP_TIMESTAMP_FORMAT = "yyyymmddHHmmss";
	
	/*
	@Override
	public void getStatus(Report report) {
		listener.getStatus(report);
		report.addElement("Dump filename", dumpFilename);
		report.addElement("Acknowledge", acknowledger);
		report.addElement("Largest pool", executor.getLargestPoolSize());
		report.addElement("Current pool", executor.getPoolSize());
		report.addElement("Active threads", executor.getActiveCount());
		report.addElement("Largest pool size", executor.getLargestPoolSize());
		report.addElement("Largest pool size", executor.getLargestPoolSize());
		report.addElement("Listener", listener);
	}*/

	public OsgiHl7Listener(){
		//System.err.println("OsgiHl7Listener()");
	}
	
	/**
	 * Opens a HL7 dump file. 
	 * @param dumpFile Filename to open. %T is substituted by the current timestamp. TODO: substitute system properties ${propertyname}
	 * @param dumpCompression compression. Supported values are 'gzip' and 'plain'.
	 * @param properties
	 * @throws IOException
	 */
	private void openDumpFile(String dumpFile, String dumpCompression, Map<String,Object> properties) throws IOException{
		String path = dumpFile.toString();
		SimpleDateFormat sdf = new SimpleDateFormat(DUMP_TIMESTAMP_FORMAT);
		path = path.replace("%T", sdf.format(new Date()));
		path = path.replace("%%", "%");
		Pattern trailingNumber = Pattern.compile("\\.[0-9]+$");
		
		File file = new File(path);
		while( file.exists() ){
			// find trailing number
			Matcher matcher = trailingNumber.matcher(path);
			String match = "";
			if( matcher.matches() ){
				match = matcher.group();
				// don't use trailing timestamp 
				if( match.length() == DUMP_TIMESTAMP_FORMAT.length()+1 ){
					match = "";
				}
			}
			int num = 0;
			if( match.length() > 0 ){
				num = Integer.parseInt(match.substring(1));
				num ++;
				path = path.substring(0, path.length()-match.length())+"."+num;
			}else{
				path = path+"."+num;
			}
			file = new File(path);
		}
		if( dumpCompression == null || dumpCompression.equals("plain") ){
			this.dumpFile = new MLLFileMessageHandler(file);
		}else if( dumpCompression.equals("gzip") ){
			this.dumpFile = MLLFileMessageHandler.GZippedHandler(file);
		}else{
			throw new IllegalArgumentException("Compression '"+dumpCompression+"' not supported. Use 'plain' or 'gzip'");
		}
		//this.dumpFilename = path;
	}
	protected void activate(Map<String,Object> config)throws IOException{
		int port = Integer.parseInt(config.get("port").toString());
		String ack = config.get("acknowledge").toString();
		
		//System.err.println("OsgiHl7Listener.activate(port="+port+", ack="+ack+")");

		if( ack == null || ack.equals("null") ){
			acknowledger = new NoAcknowledgement();
		}else if( ack.equals("mllp0x06") ){
			acknowledger = ConstantAcknowledger.ACCEPTANY_0x06;
		}else if( ack.equals("hl7v2aa") ){
			acknowledger = new SimpleHL7Acknowledger();
		}else{
			throw new UnsupportedOperationException("unsupported acknowledge: "+ack);
		}
		
		Object dumpFile = config.get("dumpFile");
		if( dumpFile != null && dumpFile.toString().length() == 0 )
			dumpFile = null;
		
		Object dumpCompression = config.get("dumpCompression");
		if( dumpCompression == null )dumpCompression = "plain";
		
		if( dumpFile != null )
			openDumpFile(dumpFile.toString(), dumpCompression.toString(), config);
		

		
		// TODO: load time from configuration
		shutdownWaitTime = 60000;
		
		// create thread group
		poolThreads = new ThreadGroup("Socket connection handlers for port "+port);
		poolThreads.setDaemon(false);
		
		// create executor
		executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 10, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(),this,this);
		
		listener = new SocketListener(port, acknowledger, executor);
		listener.setParser(parser);
		
		// set dump file if available
		if( this.dumpFile != null ){
			listener.setDumpFile(this.dumpFile);
		}
		
		listener.open();
		
		this.thread = new Thread(listener);
		this.thread.start();
	}
	
	// TODO: implement modified(Map<>) to allow real-time preferences change
	
	protected void deactivate(){
		//System.err.println("OsgiHl7Listener.deactivate");
		
		// stop processing new connections
		executor.shutdown();
		
		// TODO: notify threads to finish processing
		
		
		// stop listening and wait for the listener to exit.
		listener.close();
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO: log warning
		}
		listener = null;
		thread = null;
		
		// wait for active connections to finish processing
		boolean completed = false;
		try {
			completed = executor.awaitTermination(shutdownWaitTime, TimeUnit.MILLISECONDS);		
		} catch (InterruptedException e) {
			// TODO: log warning
		}
		
		if( completed == true ){
			executor = null;
			poolThreads.destroy();
		}else{
			// TODO: log warning
		}

		// close dump file if open
		if( dumpFile != null ){
			dumpFile.close();
		}
		
		acknowledger = null;
	}
	
	protected void bindParser(HL7Parser parser){
		//System.err.println("bindParser:"+parser.toString());
		this.parser = parser;
	}
	protected void unbindParser(HL7Parser parser){
		//System.err.println("unbindParser:"+parser.toString());
		this.parser = null;
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		// TODO log warning
	}

	@Override
	public Thread newThread(Runnable r) {
		PoolThread t = new PoolThread(poolThreads,r);
		if( t.isDaemon() )t.setDaemon(false);
		
		return t;
	}


	
}