package de.sekmi.histream.etl.filter;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.time.Instant;

import javax.script.ScriptException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.config.Meta;
import de.sekmi.histream.scripting.AbstractFacts;
import de.sekmi.histream.scripting.EncounterScriptEngine;

@XmlType(name="javascript")
public class ScriptFilter extends PostProcessingFilter{

	/** 
	 * Character encoding for an external script file 
	 */
	@XmlAttribute
	String charset;
	

	/**
	 * Specifies the URL of an external script file. The URL can be relative
	 * to the configuration.
	 */
	@XmlAttribute
	String src;
	
	/**
	 * Literal script source
	 */
	@XmlElement
	String script;


	@XmlTransient
	private int loadedIndex;
	@XmlTransient
	private EncounterScriptEngine engine;

	public Reader openReader(Meta meta) throws IOException{
		if( script != null ){
			return new StringReader(script);
		}else if( src != null ){
			URL url = new URL(meta.getLocation(), src);
			return new InputStreamReader(url.openStream(), this.charset);
		}else{
			return null;
		}
	}
	
	public Instant getTimestamp(Meta meta) throws IOException{
		// TODO use last modified of external script files
		return Instant.ofEpochMilli(meta.getLastModified());
	}
	public void loadIntoEngine(EncounterScriptEngine engine, Meta meta) throws IOException{
		this.engine = engine;
		try( Reader r = openReader(meta) ){
			this.loadedIndex = engine.addScript(r, meta.getSourceId(), getTimestamp(meta));
		} catch (ScriptException e) {
			throw new IOException("Script error in filter[type=javascript]", e);
		}
		
	}

	@Override
	public void processVisit(AbstractFacts facts) throws IOException {
		try {
			engine.processSingle(facts, loadedIndex);
		} catch (ScriptException e) {
			throw new IOException("Script execution failed", e);
		}
	}


}
