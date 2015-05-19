package de.sekmi.histream.ontology;

import java.util.Locale;


public interface Concept {
	Concept[] getNarrower() throws OntologyException;
	Concept[] getBroader() throws OntologyException;
	String[] getIDs() throws OntologyException;
	String getPrefLabel(Locale locale) throws OntologyException;
	String getDescription(Locale locale) throws OntologyException;
}
