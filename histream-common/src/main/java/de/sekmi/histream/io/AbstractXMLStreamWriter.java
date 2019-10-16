package de.sekmi.histream.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

public class AbstractXMLStreamWriter implements Closeable {
	private boolean writeFormatted;
	private int formattingDepth;
	

	/**
	 * Used to output XML
	 */
	public XMLStreamWriter writer;

	private AbstractXMLStreamWriter() {
		this.writeFormatted = true;
		this.formattingDepth = 0;		
	}
	public AbstractXMLStreamWriter(XMLStreamWriter writer) {
		this();
		this.writer = writer;
	}

	public AbstractXMLStreamWriter(OutputStream output) throws XMLStreamException {
		this();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		// enable repairing namespaces to remove duplicate namespace declarations by JAXB marshal
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		this.writer = factory.createXMLStreamWriter(output);
	}

	public AbstractXMLStreamWriter(Result result) throws XMLStreamException {
		this();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		// enable repairing namespaces to remove duplicate namespace declarations by JAXB marshal
		// this does not work with the DOM stream writer
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		this.writer = factory.createXMLStreamWriter(result);
	}
	
	/**
	 * Configure whether to write whitespace and newline for human readable output
	 * @param formattedOutput true for human readable output
	 */
	public void setFormatted(boolean formattedOutput){
		this.writeFormatted = formattedOutput;
	}
	public void formatNewline() throws XMLStreamException{
		if( writeFormatted )writer.writeCharacters("\n");
	}
	public void formatIndent() throws XMLStreamException{
		if( writeFormatted )for( int i=0; i<formattingDepth; i++ ){
			writer.writeCharacters("\t");
		}
	}
	public void formatPush(){
		formattingDepth ++;
	}
	public void formatPop(){
		formattingDepth --;
	}
	@Override
	public void close() throws IOException {
		try {
			writer.close();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	public void writeCommentLine(String comment) throws XMLStreamException {
		formatIndent();
		writer.writeComment(comment);
		formatNewline();
	}

}
