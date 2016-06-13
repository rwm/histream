package de.sekmi.histream.inference;

import java.util.Iterator;

public interface InferredConcept {

	public String getConceptId();
	
	public Iterator<String> getDependencyIDs();
}
