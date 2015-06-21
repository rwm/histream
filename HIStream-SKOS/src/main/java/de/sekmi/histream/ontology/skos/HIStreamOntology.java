package de.sekmi.histream.ontology.skos;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class HIStreamOntology {
	public static final String DWH_NAMESPACE = "http://sekmi.de/histream/dwh#";
	
	public static final URI DWH_RESTRICTION;
	
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();
		DWH_RESTRICTION = f.createURI(DWH_NAMESPACE, "restriction");
		
	}
}
