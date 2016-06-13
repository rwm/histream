package de.sekmi.histream.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.inference.EncounterInferenceEngine;
import de.sekmi.histream.inference.EncounterInferenceFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class JSEncounterInferenceFactory implements EncounterInferenceFactory{
	private ScriptEngineManager sem;
	private ObservationFactory os;
	private List<Script> scripts;
	
	public JSEncounterInferenceFactory() {
		sem = new ScriptEngineManager();
		this.scripts = new ArrayList<>();
	}
	
	private static class Script{
		URL location;
		String charset;
		String[] produces;
		String[] requires;
		public Script(URL location, String charset){
			this.location = location;
			this.charset = charset;
		}
	}
	public void addScript(URL scriptLocation, String charsetName) throws IOException, ScriptException{
		ScriptEngine meta = sem.getEngineByName("nashorn");
		// TODO use script context for control of error writer and bindings
		Bindings bindings = meta.createBindings();
		try(
				InputStream in = scriptLocation.openStream();
				Reader reader = new InputStreamReader(in, charsetName)
		){
			meta.eval(reader, bindings);			
		}
		Script script = new Script(scriptLocation, charsetName);
		// retrieve metadata
		Object p = bindings.get("produces");
		if( p == null ){
			throw new ScriptException("this.produces not defined", scriptLocation.toString(), -1);
		}else if( p instanceof ScriptObjectMirror ){
			ScriptObjectMirror som = (ScriptObjectMirror)p;
			script.produces = som.values().stream()
					.map(o -> o.toString())
					.toArray(i -> new String[i]);
		}else{
			throw new ScriptException("Unable to read this.produces of type "+p.getClass(), scriptLocation.toString(), -1);
		}

		Object r = bindings.get("requires");
		if( r == null ){
			throw new ScriptException("this.requires not defined", scriptLocation.toString(), -1);
		}else if( r instanceof ScriptObjectMirror ){
			ScriptObjectMirror som = (ScriptObjectMirror)r;
			script.requires = som.values().stream()
					.map(o -> o.toString())
					.toArray(i -> new String[i]);
		}else{
			throw new ScriptException("Unable to read this.requires of type "+p.getClass(), scriptLocation.toString(), -1);
		}
		scripts.add(script);
	}
	@Override
	public void setObservationFactory(ObservationFactory factory) {
		this.os = factory;
	}

	@Override
	public boolean canInfer(String inferredConceptId) {
		// TODO use hashmap for more efficient lookups
		for( Script s : scripts ){
			for( String prod : s.produces ){
				if( prod.equals(inferredConceptId) ){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public EncounterInferenceEngine createEngine(String inferredConceptId) {
		// TODO Auto-generated method stub
		return null;
	}

}
