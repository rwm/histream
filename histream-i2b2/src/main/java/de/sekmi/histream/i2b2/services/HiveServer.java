package de.sekmi.histream.i2b2.services;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.annotation.Resource;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;


/**
 * Provides REST interfaces to emulate an i2b2 server.
 * @author Raphael
 *
 */
@WebServiceProvider()
@ServiceMode(value = Service.Mode.MESSAGE)
public class HiveServer implements Provider<Source>{
	private ScriptEngineManager scriptManager;
	private ScriptEngine scriptEngine;
	private Path scriptDir;
	private WatchService watcher;

	@Resource
    protected WebServiceContext context;

	
	public HiveServer() {
		scriptManager = new ScriptEngineManager();
		// set bindings for callbacks to java code 
		//scriptManager.setBindings(bindings);
		//scriptEngine = scriptManager.getEngineByName("nashorn");
		// load scripts
	}
	public void loadMainScript() throws IOException, ScriptException{
		scriptEngine = scriptManager.getEngineByName("nashorn");
		try( Reader scriptFile = new FileReader(scriptDir.resolve("main.js").toFile()) ) {
			scriptEngine.eval(scriptFile);
			Object o = scriptEngine.eval("typeof httpRequest === 'function'");
			if( o == null || !(o instanceof Boolean) || ((Boolean)o) != true ){
				throw new ScriptException("global function 'httpRequest(?,?,?,?)' needed");
			}
		}
	}
	
	public void reloadChangesLoop(){
		while( true ){
			WatchKey key;
			try {
				key = watcher.take();				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			System.out.println("Reloading scripts..");
			try {
				loadMainScript();
			} catch (IOException | ScriptException e1) {
				System.err.println("Error loading script");
				e1.printStackTrace();
			}
			// pollEvents() is needed for the key to reset properly
			for( WatchEvent<?> e : key.pollEvents() ){
				if( e == null )continue;// noop
				//System.out.println("Event: "+e.kind().toString() + ", "+e.toString());
			}
			if( !key.reset() ){
				System.err.println("Key closed");
				break;
			}
		}
	}
	@SuppressWarnings("resource") // cannot close default filesystem
	public void loadScipts()throws IOException, ScriptException{
		
		FileSystem fs = FileSystems.getDefault();
		scriptDir = fs.getPath("src", "main", "scripts", "i2b2-ws");
		if( !Files.isDirectory(scriptDir) )throw new FileNotFoundException("Script dir not found: "+scriptDir);
		loadMainScript();
		// TODO: watch directory and reload script if changes
		watcher = fs.newWatchService();
		scriptDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
		
		/*
		try( DirectoryStream<Path> dir = Files.newDirectoryStream(scriptDir, "*.js") ){
			for( Path script : dir ){
				System.out.println("Script: "+script.normalize().toString());
				ScriptEngine se = scriptManager.getEngineByName("nashorn");
				try {
					se.eval(new FileReader(script.toFile()));
					System.out.println("cell.name="+se.eval("this.cell.id").toString());
				} catch (ScriptException e) {
					e.printStackTrace();
				}
			}
		}*/
	}
	public static void main(String args[]) throws IOException, ScriptException{
		HiveServer hs = new HiveServer();
		hs.loadScipts();
		Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, hs);
		// use executor for more control over parallel executions
		//e.setExecutor(/*...*/);
		String address = "http://localhost:9000/i2b2/services";
		e.publish(address);
		
		hs.reloadChangesLoop();
		
	}

	@Override
	public StreamSource invoke(Source request) {
		// TODO Auto-generated method stub
	    MessageContext mc = context.getMessageContext();
        String path = (String)mc.get(MessageContext.PATH_INFO);
        String query = (String)mc.get(MessageContext.QUERY_STRING);
        String httpMethod = (String)mc.get(MessageContext.HTTP_REQUEST_METHOD);
        if( request != null ){
        	System.out.println("Source: "+request.getClass());
        }
        if( scriptEngine != null )try {
			Object ret = ((Invocable)scriptEngine).invokeFunction("httpRequest", httpMethod, path, query, request);
			return new StreamSource(new StringReader(ret.toString()));
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        /*
        @SuppressWarnings("unchecked")
		java.util.Map<java.lang.String, java.util.List<java.lang.String>> headers =
        	(Map<String, List<String>>) context.getMessageContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
        headers.put("Content-Type", Arrays.asList("text/html"));*/
        return new StreamSource(new StringReader("<!DOCTYPE html><html><head></head><body>ERROR</body></html>"));
	}
}
