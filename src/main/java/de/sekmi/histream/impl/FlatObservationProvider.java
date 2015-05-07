package de.sekmi.histream.impl;

import java.util.Hashtable;
import java.util.Map;

import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

/**
 * Reads a flat file with content type text/tab-separated-values.
 * @author Raphael
 *
 */
public class FlatObservationProvider extends AbstractObservationProvider implements FlatObservationWriter {
	static private Class<?>[] supportedExtensions = new Class<?>[]{Patient.class,Visit.class};

	private Map<String, String> meta;
	private Map<String, String> conceptMap;
	
	public FlatObservationProvider() {
		meta = new Hashtable<>();
	}
	@Override
	public Class<?>[] getSupportedExtensions() {
		return supportedExtensions;
	}
	
	@Override
	public void writeMeta(String key, String value) {
		meta.put(key, value);
	}
	@Override
	public void writeConceptMap(String concept, String map) {
		conceptMap.put(concept, map);
	}
	@Override
	public void beginGroup() {
		// TODO build group of following observations
		
	}
	@Override
	public void endGroup() {
		// TODO group finished, provide observation
		
	}
	@Override
	public void writeObservation(String[] fields) {
		// TODO Auto-generated method stub
		
	}
	

	
}
