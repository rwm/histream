package de.sekmi.histream.io;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.ParseException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;

@Deprecated
public class XMLObservationSupplier extends XMLObservationParser implements ObservationSupplier{
	//private static final String namespaceURI = "http://sekmi.de/histream/dwh-eav";
	protected XMLStreamReader reader;
	
	private AttributeAccessor atts;
	
	public XMLObservationSupplier(ObservationFactory factory, XMLStreamReader reader) throws XMLStreamException {
		setObservationFactory(factory);
		this.reader = reader;
		atts = new AttributeAccessor() {
			@Override
			public String getValue(String name) {
				// no need for namespace attributes
				return reader.getAttributeValue(null, name);
			}
		};
		// read start of document until start of visit
		readToRoot();
		readMeta();
		readVisit();
	}
	public XMLObservationSupplier(ObservationFactory factory, InputStream input) throws XMLStreamException, FactoryConfigurationError {
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
		if( reader.getLocalName().equals("etl") ){
			String etlStrategy = reader.getAttributeValue(null, "strategy");
			// TODO use constants for etl.strategy, etc.
			if( etlStrategy != null )setMeta(ObservationSupplier.META_ETL_STRATEGY, etlStrategy);
			reader.nextTag();
			// should be end element
			reader.nextTag();
		}
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
	protected void readVisit()throws XMLStreamException{
		if( !reader.getLocalName().equals("visit") )throw new XMLStreamException("Element visit expected instead of "+reader.getLocalName(),reader.getLocation());
		reader.nextTag();
		while( !reader.getLocalName().equals("facts") ){
			// read visit attributes

			if( reader.getLocalName().equals("encounter") ){
				try {
					parseEncounter(atts);
				} catch (ParseException e) {
					throw new XMLStreamException("Unable to parse encounter", reader.getLocation(), e);
				}
			}

			String text = reader.getElementText();
			visitData.put(reader.getLocalName(), text);
			
			// go to next element start or </facts>
			reader.nextTag();
		}
		
		reader.nextTag();
		// should be an observation
	}
	
	protected Observation readObservation()throws XMLStreamException{
		// </facts> might occur after previous call to readObservation()
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
		try {
			newObservation(atts);
		} catch (ParseException e) {
			throw new XMLStreamException("Unable to parse observation", reader.getLocation(), e);
		}
		if( reader.getLocalName().equals("eav-group") ){
			// group item can have value
			parseValueAttributes(atts);
			fact.setValue(parseValue(atts.getValue("value")));
			
			// parse modifiers
			reader.nextTag();
			while( reader.isStartElement() ){
				if( !reader.getLocalName().equals("value") )throw new XMLStreamException("Only element 'value' allowed in eav-group",reader.getLocation());
				
				parseValueAttributes(atts);

				modifier = fact.addModifier(atts.getValue("modifier"), parseValue(reader.getElementText()));
				
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
	@Override
	public void close() throws XMLStreamException{
		reader.close();
	}

}
