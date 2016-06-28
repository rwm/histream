package de.sekmi.histream.inference;

import java.util.Iterator;

public interface InferredConcept {

	// TODO need producing of multiple concepts?
	public String getConceptId();
	
	public Iterator<String> getDependencyIDs();
}
