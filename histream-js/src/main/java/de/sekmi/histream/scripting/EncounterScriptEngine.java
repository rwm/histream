package de.sekmi.histream.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
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


public class EncounterScriptEngine {
	private ScriptEngine engine;
	private List<CompiledScript> scripts;
	private ObservationFactory factory;
	
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

	public void addScript(String script) throws ScriptException{
		scripts.add(((Compilable)engine).compile(script));
	}
	public void addScript(URL location, String charset) throws ScriptException, IOException{
		try(
				InputStream in = location.openStream();
				Reader reader = new InputStreamReader(in, charset)
		){
			addScript(reader);
		}
	}
	public void addScript(Reader reader) throws ScriptException{
		scripts.add(((Compilable)engine).compile(reader));	
	}
	
	public void setObservationFactory(ObservationFactory factory){
		this.factory = factory;
	}
	public void processEncounter(String patientId, String encounterId, DateTimeAccuracy defaultStartTime, List<Observation> facts) throws ScriptException{
		Facts f = new Facts(factory, patientId, encounterId, defaultStartTime);
		f.setObservations(facts);
		Bindings b = engine.createBindings();
		b.put("facts", f);
		for( CompiledScript script : scripts ){
			script.eval(b);
		}
		// TODO is there a way to add information which script threw an exception?
	}
}
