package de.sekmi.histream.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.util.function.Consumer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationParser;
import de.sekmi.histream.ObservationProvider;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.AbstractValue;

/**
 * Sax2 content handler which reads an xml eav representation 
 * and provides a stream of observations as the xml is processed.
 * <p>
 * Very large files can be processed, as the observations are processed sequentially when
 * they are read.
 * 
 * @author Raphael
 *
 */
public class SAXObservationProvider extends XMLObservationParser implements ContentHandler, ObservationProvider, ObservationParser{
	//private static final Logger log = Logger.getLogger(SAXObservationProvider.class.getName());

	static private Class<?>[] supportedExtensions = new Class<?>[]{Patient.class,Visit.class};
	// TODO: also support Concept
	//private Consumer<Visit> beforeFacts;
	private Consumer<Observation> handler;
	
	private enum Section { Root, Meta, Visit, Data };
	private Section section;
	private CharBuffer buffer;
	
	/*// no need for fast access, reading from file is slow anyways
	private ExtensionAccessor<Visit> visit;
	private ExtensionAccessor<Patient> patient;
	*/
	
	public SAXObservationProvider() {
		buffer = CharBuffer.allocate(1024);
	}
	
	@Override
	public void characters(char[] ch, int start, int length)throws SAXException {
		try{
			buffer.put(ch, start, length);
		}catch( BufferOverflowException e ){
			throw new SAXException(e);
		}
	}


	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName)throws SAXException {
		buffer.flip();
		switch( section ){
		case Data:
			if( qName.equals("eav-item") || qName.equals("eav-group") ){
				// process observation
				if( qName.equals("eav-item") ){
					fact.setValue(parseValue(buffer.toString()));
				}
				provideObservation(fact);
				fact = null;
			}else if( qName.equals("value") ){
				// modifier value parsed
				modifier.setValue(parseValue(buffer.toString()));
			}
		case Meta:
			if( qName.equals("meta") ){
				section = Section.Root;
			}else if( qName.equals("enum") ){
				// TODO: create enum concept and store in hashtable
			}
			break;
		case Visit:
			// all other fields are stored with the corresponding element name
			visitData.put(qName, buffer.toString());
			break;
		case Root:
			// nothing
			break;
		}
		buffer.clear();
		
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)throws SAXException {}

	@Override
	public void processingInstruction(String target, String data)throws SAXException {}

	@Override
	public void setDocumentLocator(Locator locator) {}

	@Override
	public void skippedEntity(String name) throws SAXException {}

	@Override
	public void startDocument() throws SAXException {
		section = Section.Root;
		visitData.clear();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		AttributeAccessor atts = new AttributeAccessor(){
			@Override
			public String getValue(String name) {
				return attributes.getValue(name);
			}
		};
		buffer.clear();
		switch( section ){
		case Data:
			if( qName.equals("eav-item") ){
				newObservation(atts);
				parseValueAttributes(atts);
			}else if( qName.equals("eav-group") ){
				newObservation(atts);
				fact.setValue(AbstractValue.NONE);
			}else if( qName.equals("value") ){
				modifier = fact.addModifier(atts.getValue("modifier"));
				parseValueAttributes(atts);
			}
			break;
		case Meta:
			if( qName.equals("source") ){
				parseSource(atts);
			}
			break;
		case Visit:
			if( qName.equals("encounter") ){
				parseEncounter(atts);
			}else if( qName.equals("facts") ){
				// visit section is complete
				patientId = visitData.get("patid");
				
				
				// patient and visit objects are created, once the first fact is complete
				// data section begins
				section = Section.Data;
			}
			break;
		case Root:
			if( qName.equals("meta") ){
				section = Section.Meta;
			}else if( qName.equals("visit") ){
				section = Section.Visit;
			}
			break;
		default:
			break;
		}
		
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {}

	@Override
	public Class<?>[] getSupportedExtensions() {
		return supportedExtensions;
	}
	

	@Override
	public void parse(InputStream input) throws IOException{
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(this);
			reader.parse(new InputSource(input));
		} catch (SAXException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void setHandler(Consumer<Observation> handler) {
		this.handler = handler;
	}
	
	private void provideObservation(Observation observation){
		handler.accept(observation);
	}

	@Override
	public void setObservationFactory(ObservationFactory factory) {
		this.factory = factory;
	}
}
