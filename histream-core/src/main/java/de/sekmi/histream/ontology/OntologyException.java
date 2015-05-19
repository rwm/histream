package de.sekmi.histream.ontology;

public class OntologyException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OntologyException(Throwable cause){
		super(cause);
	}
	
	public OntologyException(String message){
		super(message);
	}
}
