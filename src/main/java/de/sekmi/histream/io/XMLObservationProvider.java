package de.sekmi.histream.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;






import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.AbstractValue;

public class XMLObservationProvider extends XMLObservationParser implements FileObservationProvider{
	//private static final String namespaceURI = "http://sekmi.de/histream/dwh-eav";
	private XMLStreamReader reader;
	private boolean documentStart;
	
	private AttributeAccessor atts;
	
	public XMLObservationProvider(ObservationFactory factory, XMLStreamReader reader) {
		setObservationFactory(factory);
		this.reader = reader;
		atts = new AttributeAccessor() {
			@Override
			public String getValue(String name) {
				// no need for namespace attributes
				return reader.getAttributeValue(null, name);
			}
		};
		documentStart = true;
	}
	public XMLObservationProvider(ObservationFactory factory, InputStream input) throws XMLStreamException, FactoryConfigurationError {
		this(factory, XMLInputFactory.newInstance().createXMLStreamReader(input));
	}
	
	private void readToRoot() throws XMLStreamException{
		while( reader.hasNext() ){
			reader.next();
			if( reader.isStartElement() )break;
		}
		if( !reader.isStartElement() || !reader.getLocalName().equals("dwh-eav") ){
			throw new XMLStreamException("Start element 'dwh-eav' expected instead of "+reader.getLocalName(), reader.getLocation());
		}
		//String etlStrategy = reader.getAttributeValue(namespaceURI, "etl-strategy");
		
		reader.nextTag();
	}
	
	private void readMeta()throws XMLStreamException{
		if( !reader.isStartElement() || !reader.getLocalName().equals("meta") )return;
		
		// read meta
		reader.nextTag();
		if( reader.getLocalName().equals("source") ){
			parseSource(atts);
			reader.nextTag();
			// should be end element
			reader.nextTag();
		}
		/*
		if( reader.getLocalName().equals("provider") ){
			reader.getAttributeValue(namespaceURI, "id");
			// read value
			reader.getElementText();
			reader.nextTag(); // should find end element
			reader.nextTag();
		}*/
		
		
		
		// skip to end of meta
		while( !reader.isEndElement() || !reader.getLocalName().equals("meta") ){
			reader.next();
		}
		reader.nextTag();

	}
	private void readVisit()throws XMLStreamException{
		if( !reader.getLocalName().equals("visit") )throw new XMLStreamException("Element visit expected instead of "+reader.getLocalName(),reader.getLocation());
		reader.nextTag();
		while( !reader.getLocalName().equals("facts") ){
			// read visit attributes

			if( reader.getLocalName().equals("encounter") ){
				parseEncounter(atts);
			}

			String text = reader.getElementText();
			visitData.put(reader.getLocalName(), text);
			
			// go to next element start or </facts>
			reader.nextTag();
		}
		reader.nextTag();
		// should be an observation
	}
	
	private Observation readObservation()throws XMLStreamException{
		if( documentStart ){
			readToRoot();
			readMeta();
			readVisit();
			documentStart = false;
		}
		while( reader.isEndElement() ){
			switch( reader.getLocalName() ){
			case "facts":
				// end of facts
				reader.nextTag();
			case "visit":
				// end of visit
				reader.nextTag();
				if( reader.isStartElement() && reader.getLocalName().equals("visit") ){
					// next visit
					readVisit();
				}
				break;
			case "dwh-eav":
				// end of document
				return null;
			}
		}
		// start element of eav-item or eav-group
		if( !reader.isStartElement() 
				||!(reader.getLocalName().equals("eav-item")
				|| reader.getLocalName().equals("eav-group")) ){
			throw new XMLStreamException("Element eav-item or eav-group expected instead of "+reader.getLocalName(), reader.getLocation());
		}
		newObservation(atts);
		if( reader.getLocalName().equals("eav-group") ){
			// no value for group item
			fact.setValue(AbstractValue.NONE);
			
			// parse modifiers
			reader.nextTag();
			while( reader.isStartElement() ){
				if( !reader.getLocalName().equals("value") )throw new XMLStreamException("Only element 'value' allowed in eav-group",reader.getLocation());
				
				modifier = fact.addModifier(atts.getValue("modifier"));
				
				parseValueAttributes(atts);
				modifier.setValue(parseValue(reader.getElementText()));
				
				reader.nextTag();
			}
			// should be at end element of eav-group
		}else{
			// parse eav-item value
			parseValueAttributes(atts);
			fact.setValue(parseValue(reader.getElementText()));
			// should be at end element of eav-item
		}
		// go to next observation or </facts>
		reader.nextTag();
		
		Observation obs = fact;
		fact = null;
		return obs;
	}
	
	@Override
	public Observation get() {
		Observation o;
		try {
			o = readObservation();
		} catch (XMLStreamException e) {
			throw new UncheckedIOException(new IOException(e));
		}
		return o;
	}

}
