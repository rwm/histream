package de.sekmi.histream.ontology;

import java.util.Locale;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

public interface ValueRestriction {
	QName getType();
	Object[] getEnumerationValues();
	String[] getEnumerationLabels(Locale locale) throws OntologyException;
	Number minInclusive();
	Number maxInclusive();
	Integer minLength();
	Integer maxLength();
	Pattern getPattern();

	// TODO implement validation (maybe necessary to separate enum values and enum labels)
	//boolean validate(Value value);
	
	public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema#"; 

	public static final QName TYPE_DECIMAL = new QName(XSD_NAMESPACE,"decimal");
	public static final QName TYPE_INTEGER = new QName(XSD_NAMESPACE,"integer");
	public static final QName TYPE_STRING = new QName(XSD_NAMESPACE,"string");
	public static final QName TYPE_FLOAT = new QName(XSD_NAMESPACE,"float");
	
	
}
