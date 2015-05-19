package de.sekmi.histream.ontology;

import java.io.Closeable;

public interface Ontology extends Closeable{
	public Concept getConceptByNotation(String id) throws OntologyException;
	public Concept[] getTopConcepts() throws OntologyException;
}
