package de.sekmi.histream.etl.filter;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.script.ScriptException;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.VisitPostProcessorQueue;
import de.sekmi.histream.etl.config.DataSource;
import de.sekmi.histream.scripting.AbstractFacts;
import de.sekmi.histream.scripting.EncounterScriptEngine;

public class FilterPostProcessingQueue extends VisitPostProcessorQueue {
	private EncounterScriptEngine engine;
	private PostProcessingFilter[] filters;

	public FilterPostProcessingQueue(DataSource ds, ObservationFactory factory) throws IOException {
		try {
			engine = new EncounterScriptEngine();
		} catch (ScriptException e) {
			throw new IOException("Unable to create script engine", e);
		}
		engine.setObservationFactory(factory);

		filters = ds.getPostProcessingFilters();
		// load script files into engine
		for( int i=0; i<filters.length; i++ ){
			if( filters[i] instanceof ScriptFilter ){
				((ScriptFilter)filters[i]).loadIntoEngine(engine, ds.getMeta());
			}
		}

		// load scripts
//		for( int i=0; i<ds.length; i++ ){
//			try( Reader r = scripts[i].openReader(meta) ){
//				engine.addScript(r, meta.getSourceId(), scripts[i].getTimestamp(meta));
//			} catch (ScriptException e) {
//				throw new IOException("Script error in script "+i, e);
//			}
//		}
	}
	@Override
	protected void postProcessVisit() {
		if( getVisit() == null ){
			return; // don't want null visits
		}
		AbstractFacts facts = engine.wrapEncounterFacts(getPatient(), getVisit(), getVisitFacts());
		
		for( int i=0; i<filters.length; i++ ){
			try {
				filters[i].processVisit(facts);
			} catch (IOException e) {
				// TODO UncheckedIOException might be unwrapped and message might be lost, verify this
				throw new UncheckedIOException("Filter execution failed for patient="+getPatient().getId()+", visit="+getVisit().getId(), e);
			}
		}
	}
	
	public int getNumScripts(){
		return engine.getScriptCount();
	}

}
