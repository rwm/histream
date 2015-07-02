package de.sekmi.histream.ontology.skos;

import org.openrdf.model.Resource;

import de.sekmi.histream.ontology.OntologyException;

public class SKOSException extends OntologyException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Resource node;
	
	public SKOSException(Resource node, String message){
		super(message);
		this.node = node;
	}
	public SKOSException(Throwable cause) {
		super(cause);
	}
	
	public String toString(){
		return "SKOSException for Node "+node+": "+getMessage();
	}

}
