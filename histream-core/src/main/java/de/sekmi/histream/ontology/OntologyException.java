package de.sekmi.histream.ontology;

/**
 * Ontology exception
 * 
 * @author Raphael
 *
 */
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
