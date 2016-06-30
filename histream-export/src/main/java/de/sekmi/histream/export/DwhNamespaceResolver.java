package de.sekmi.histream.export;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import de.sekmi.histream.io.GroupedXMLWriter;

public class DwhNamespaceResolver implements NamespaceContext{

	@Override
	public String getNamespaceURI(String prefix) {
		if( prefix == null ){
			throw new IllegalArgumentException("prefix is null");
		}else if( prefix.equals(XMLConstants.DEFAULT_NS_PREFIX) ){
			return GroupedXMLWriter.NAMESPACE;
		}else{
			return XMLConstants.NULL_NS_URI;
		}
	}

	@Override
	public String getPrefix(String namespaceURI) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		throw new UnsupportedOperationException();
	}

}
