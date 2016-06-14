package de.sekmi.histream.etl;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;

import javax.script.ScriptException;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.config.Meta;
import de.sekmi.histream.etl.config.Script;
import de.sekmi.histream.scripting.EncounterScriptEngine;

public class ScriptProcessingQueue extends VisitPostProcessorQueue {
	private EncounterScriptEngine engine;

	public ScriptProcessingQueue(Script[] scripts, Meta meta, ObservationFactory factory) throws IOException {
		try {
			engine = new EncounterScriptEngine();
		} catch (ScriptException e) {
			throw new IOException("Unable to create script engine", e);
		}
		engine.setObservationFactory(factory);

		// load scripts
		for( int i=0; i<scripts.length; i++ ){
			try( Reader r = scripts[i].openReader(meta) ){
				engine.addScript(r, meta.getSourceId(), scripts[i].getTimestamp(meta));
			} catch (ScriptException e) {
				throw new IOException("Script error in script "+i, e);
			}
		}
	}
	@Override
	protected void postProcessVisit() {
		if( getVisit() == null ){
			return; // don't want null visits
		}
		try {
			engine.processEncounter(getPatient(), getVisit(), getVisitFacts());
		} catch (ScriptException e) {
			IOException io = new IOException("Error during script execution for patient="+getPatient().getId()+", visit="+getVisit().getId(), e);
			throw new UncheckedIOException(io);
		}
	}
	
	public int getNumScripts(){
		return engine.getScriptCount();
	}

}
