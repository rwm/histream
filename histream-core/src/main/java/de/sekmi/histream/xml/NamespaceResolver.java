package de.sekmi.histream.xml;

import java.util.Arrays;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import de.sekmi.histream.impl.ObservationImpl;

/**
 * Namespace context e.g. for XPath evaluations.
 * Supports {@value ObservationImpl#XML_NAMESPACE} with default prefix
 * and second prefix {@value NamespaceResolver#DEFAULT_EAV_PREFIX}.
 * XXX a future implementation may allow changing the second prefix
 * <p>
 * Also {@code xml} and {@code xsi} prefixes.
 * </p>
 * @author R.W.Majeed
 *
 */
public class NamespaceResolver implements NamespaceContext{
	public static final String DEFAULT_EAV_PREFIX = "eav";
	// XXX maybe make the second prefix 'eav' changeable with a constructor
	
	@Override
	public Iterator<?> getPrefixes(String namespaceURI) {
		String prefix = getPrefix(namespaceURI);
		if( prefix == null )return Arrays.asList().iterator();
		else if( namespaceURI.equals(ObservationImpl.XML_NAMESPACE) ){
			return Arrays.asList(prefix, DEFAULT_EAV_PREFIX).iterator();
		}else return Arrays.asList(prefix).iterator();
	}
	
	@Override
	public String getPrefix(String namespaceURI) {
		switch( namespaceURI ){
		case ObservationImpl.XML_NAMESPACE:
			return XMLConstants.DEFAULT_NS_PREFIX;
		case XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI:
			return "xsi";
		case XMLConstants.XML_NS_URI:
			return XMLConstants.XML_NS_PREFIX;
		case XMLConstants.XMLNS_ATTRIBUTE_NS_URI:
			return XMLConstants.XMLNS_ATTRIBUTE;
		}
		return null;
	}
	
	@Override
	public String getNamespaceURI(String prefix) {
		switch( prefix ){
		case XMLConstants.DEFAULT_NS_PREFIX:
		case DEFAULT_EAV_PREFIX:
			return ObservationImpl.XML_NAMESPACE;
		case "xsi":
			return XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
		case XMLConstants.XML_NS_PREFIX:
			return XMLConstants.XML_NS_URI;
		case XMLConstants.XMLNS_ATTRIBUTE:
			return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		}
		return XMLConstants.NULL_NS_URI;
	}
}
