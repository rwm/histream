package de.sekmi.histream.ontology.skos;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class HIStreamOntology {
	public static final String DWH_NAMESPACE = "http://sekmi.de/histream/dwh#";
	
	public static final URI DWH_RESTRICTION;
	public static final URI DWH_MAPFACT;
	public static final URI DWH_CONDITION;
	public static final URI DWH_CHOOSE;
	public static final URI DWH_TARGET;
	public static final URI DWH_MODIFY;
	public static final URI DWH_OTHERWISE;
	public static final URI DWH_XPATH;
	public static final URI DWH_ECMASCRIPT;
	public static final URI DWH_HAS_PART;
	public static final URI DWH_IS_PART_OF;
	
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();
		DWH_RESTRICTION = f.createURI(DWH_NAMESPACE, "restriction");
		DWH_MAPFACT = f.createURI(DWH_NAMESPACE, "mapFact");
		DWH_CONDITION = f.createURI(DWH_NAMESPACE, "condition");
		DWH_CHOOSE = f.createURI(DWH_NAMESPACE, "choose");
		DWH_TARGET = f.createURI(DWH_NAMESPACE, "target");
		DWH_MODIFY = f.createURI(DWH_NAMESPACE, "modify");
		DWH_OTHERWISE = f.createURI(DWH_NAMESPACE, "otherwise");
		DWH_XPATH = f.createURI(DWH_NAMESPACE, "XPath");
		DWH_ECMASCRIPT = f.createURI(DWH_NAMESPACE, "ECMAScript");
		DWH_HAS_PART = f.createURI(DWH_NAMESPACE, "hasPart"); // TODO use correct names/URIs
		DWH_IS_PART_OF = f.createURI(DWH_NAMESPACE, "isPartOf"); // XXX see above
		// dwh:hasPart rdfs:subPropertyOf http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#hasPart
		
	}
}
