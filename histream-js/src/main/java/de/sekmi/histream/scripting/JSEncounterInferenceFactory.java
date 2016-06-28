package de.sekmi.histream.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import de.sekmi.histream.inference.EncounterInferenceEngine;
import de.sekmi.histream.inference.EncounterInferenceFactory;
import de.sekmi.histream.inference.InferredConcept;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class JSEncounterInferenceFactory extends EncounterInferenceFactory{
	private ScriptEngineManager sem;
	private List<Script> scripts;
	private Map<String, InferenceEntry> inferenceLookup;
	
	public JSEncounterInferenceFactory() {
		sem = new ScriptEngineManager();
		this.scripts = new ArrayList<>();
		this.inferenceLookup = new HashMap<>();
	}
	
	private static class Script{
		String script;
		String[] produces;
		String[] requires;
		public Script(String script){
			this.script = script;
		}
	}
	private static class InferenceEntry implements InferredConcept{
		private Script script;
		private int conceptIndex;
		public InferenceEntry(Script script, int conceptIndex){
			this.script = script;
			this.conceptIndex = conceptIndex;
		}
		@Override
		public Iterator<String> getDependencyIDs() {
			return Arrays.stream(script.produces).iterator();
		}
		@Override
		public String getConceptId() {
			return script.produces[conceptIndex];
		}
	}

	private String readReader(Reader reader) throws IOException{
		StringBuilder builder = new StringBuilder();
		CharBuffer buffer = CharBuffer.allocate(2048);
		while( reader.read(buffer) != -1 ){
			buffer.flip();
			builder.append(buffer.toString());
			buffer.clear();
		}
		return builder.toString();
	}

	public void addScript(URL scriptLocation, String charsetName) throws IOException, ScriptException{
		ScriptEngine meta = sem.getEngineByName("nashorn");
		// TODO use script context for control of error writer and bindings
		Bindings bindings = meta.createBindings();
		String source;
		try(
				InputStream in = scriptLocation.openStream();
				Reader reader = new InputStreamReader(in, charsetName);
		){
			source = readReader(reader);
			meta.eval(source, bindings);			
		}

		Script script = new Script(source);
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
		addScript(script);		
	}
	
	private void addScript(Script script){
		// verify that the produced concept ids are unique to this factory
		for( String id : script.produces ){
			if( inferenceLookup.containsKey(id) ){
				throw new IllegalStateException("Produced concept id '"+id+"' not unique to inference engine");
			}
		}
		for( int i=0; i<script.produces.length; i++ ){
			inferenceLookup.put(script.produces[i], new InferenceEntry(script,i));
		}
		scripts.add(script);
	}

	private Script getScript(String inferredConceptId){
		InferenceEntry e = inferenceLookup.get(inferredConceptId);
		if( e == null ){
			return null;
		}else{
			return e.script;
		}
	}
	@Override
	public boolean canInfer(String inferredConceptId) {
		return inferenceLookup.containsKey(inferredConceptId);
	}

	@Override
	public EncounterInferenceEngine createEngine(String inferredConceptId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InferredConcept getConceptById(String inferredConceptId) {
		return inferenceLookup.get(inferredConceptId);
	}

}
