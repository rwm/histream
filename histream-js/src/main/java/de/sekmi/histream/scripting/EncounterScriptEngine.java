package de.sekmi.histream.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.ExternalSourceImpl;


public class EncounterScriptEngine {
	private ScriptEngine engine;
	private List<Script> scripts;
	private ObservationFactory factory;
	
	private static class Script{
		CompiledScript script;
		ExternalSourceType source;
		
		public Script(CompiledScript script, String sourceId, Instant timestamp){
			this.script = script;
			source = new ExternalSourceImpl(sourceId, timestamp);
		}
	}
	public EncounterScriptEngine() throws javax.script.ScriptException {
		this.engine = new ScriptEngineManager().getEngineByName("nashorn");
		if( engine == null ){
			throw new ScriptException("Script engine not available");
		}else if( !(engine instanceof Compilable) ){
			throw new ScriptException("Script engine does not support compilation");
		}
		// TODO use custom context for error writer and global bindings
		
		// use strict mode
		engine.eval("'use strict';");
		scripts = new LinkedList<>();
	}

	public void addScript(String script, String sourceId, Instant timestamp) throws ScriptException{
		scripts.add(new Script(((Compilable)engine).compile(script), sourceId, timestamp));
	}
	public void addScript(URL location, String charset, String sourceId) throws ScriptException, IOException{
		URLConnection conn = location.openConnection();
		Instant timestamp = Instant.ofEpochMilli(conn.getLastModified());
		try(
				InputStream in = conn.getInputStream();
				Reader reader = new InputStreamReader(in, charset)
		){
			addScript(reader, sourceId, timestamp);
		}
	}
	public void addScript(Reader reader, String sourceId, Instant timestamp) throws ScriptException{
		scripts.add(new Script(((Compilable)engine).compile(reader), sourceId, timestamp));	
	}
	
	public int getScriptCount(){
		return scripts.size();
	}
	
	public void setObservationFactory(ObservationFactory factory){
		this.factory = factory;
	}
	private void process(AbstractFacts facts) throws ScriptException{
		Bindings b = engine.createBindings();
		b.put("facts", facts);
		for( Script script : scripts ){
			facts.setSource(script.source);
			script.script.eval(b);
		}
		// TODO is there a way to add information which script threw an exception?		
	}
	public void processEncounter(String patientId, String encounterId, DateTimeAccuracy defaultStartTime, List<Observation> facts) throws ScriptException{
		SimpleFacts f = new SimpleFacts(factory, patientId, encounterId, defaultStartTime);
		f.setObservations(facts);
		process(f);
	}
	public void processEncounter(Patient patient, Visit visit, List<Observation> facts) throws ScriptException{
		VisitExtensionFacts f = new VisitExtensionFacts(factory, patient, visit);
		f.setObservations(facts);
		process(f);
	}
}
