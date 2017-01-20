package de.sekmi.histream.export;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class NoOpStreamWriter implements XMLStreamWriter {
	public static NoOpStreamWriter INSTANCE = new NoOpStreamWriter();
	@Override
	public void writeStartElement(String localName) throws XMLStreamException {}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
	}

	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
	}

	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
	}

	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
	}

	@Override
	public void writeEndDocument() throws XMLStreamException {
	}

	@Override
	public void close() throws XMLStreamException {
	}

	@Override
	public void flush() throws XMLStreamException {
	}

	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
	}

	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
			throws XMLStreamException {
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
	}

	@Override
	public void writeComment(String data) throws XMLStreamException {
	}

	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
	}

	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
	}

	@Override
	public void writeCData(String data) throws XMLStreamException {
	}

	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
	}

	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
	}

	@Override
	public void writeStartDocument() throws XMLStreamException {
	}

	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
	}

	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
	}

	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
	}

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return null;
	}

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
	}

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
	}

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return null;
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return null;
	}

}
