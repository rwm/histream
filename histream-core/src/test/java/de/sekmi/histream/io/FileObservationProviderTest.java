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


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;
import java.math.BigDecimal;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.Value;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Modifier;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.SimplePatientExtension;
import de.sekmi.histream.impl.SimpleVisitExtension;
import de.sekmi.histream.impl.TestObservationHandler;
import de.sekmi.histream.io.AbstractObservationParser;
import de.sekmi.histream.io.FlatObservationSupplier;
import de.sekmi.histream.io.XMLObservationSupplier;


public class FileObservationProviderTest {

	private TestObservationHandler handler;
	private ObservationFactoryImpl factory;
	
	@Before
	public void initializeObservationFactory(){
		factory = new ObservationFactoryImpl();
		factory.registerExtension(new SimplePatientExtension());
		factory.registerExtension(new SimpleVisitExtension());
		//factory.registerExtension(new ConceptExtension());
		
	}	
	
	@Before
	public void initializeHandler(){
		handler = new TestObservationHandler(new TestObservationHandler.Tester[]{
			(Observation o) ->  {
				Assert.assertEquals("T:date:secs", o.getConceptId());
				Assert.assertEquals(ChronoUnit.SECONDS, o.getStartTime().getAccuracy());
				Assert.assertEquals(3, o.getStartTime().getLong(ChronoField.SECOND_OF_MINUTE));
				Patient p = o.getExtension(Patient.class);
				Assert.assertNotNull("Patient extension required", p);
				Assert.assertEquals("XX12345", p.getId());
				// TODO: test more patient information
				Visit v = o.getExtension(Visit.class);
				Assert.assertNotNull("Visit extension required", v);
				Assert.assertEquals("Zuhause", v.getLocationId());
				Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2014-01-01T10:30:00"), v.getStartTime());
				Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2014-01-05T10:30:00"), v.getEndTime());
				// TODO test visit information
			},
			(Observation o) ->  {
				Assert.assertEquals("T:date:mins", o.getConceptId());
				Assert.assertEquals(ChronoUnit.MINUTES, o.getStartTime().getAccuracy());
				Assert.assertEquals(40, o.getStartTime().getLong(ChronoField.MINUTE_OF_HOUR));
			},
			(Observation o) ->  {
				Assert.assertEquals("T:date:hours", o.getConceptId());
				Assert.assertEquals(ChronoUnit.HOURS, o.getStartTime().getAccuracy());
				Assert.assertEquals(10, o.getStartTime().getLong(ChronoField.HOUR_OF_DAY));
			},
			(Observation o) ->  {
				Assert.assertEquals("T:date:day", o.getConceptId());
				Assert.assertEquals(ChronoUnit.DAYS, o.getStartTime().getAccuracy());
				Assert.assertEquals(7, o.getStartTime().getLong(ChronoField.DAY_OF_MONTH));
			},
			(Observation o) ->  {
				Assert.assertEquals("T:date:month", o.getConceptId());
				Assert.assertEquals(ChronoUnit.MONTHS, o.getStartTime().getAccuracy());
				Assert.assertEquals(9, o.getStartTime().getLong(ChronoField.MONTH_OF_YEAR));
			},
			(Observation o) ->  {
				Assert.assertEquals("T:date:year", o.getConceptId());
				Assert.assertEquals(ChronoUnit.YEARS, o.getStartTime().getAccuracy());
				Assert.assertEquals(2014, o.getStartTime().getLong(ChronoField.YEAR));
			},
			(Observation o) ->  {
				Assert.assertEquals("T:type:str", o.getConceptId());
				Assert.assertEquals(Value.Type.Text, o.getValue().getType());
				Assert.assertEquals("abc123", o.getValue().getStringValue());
			},
			(Observation o) ->  {
				Assert.assertEquals("T:type:int", o.getConceptId());
				Assert.assertEquals(Value.Type.Numeric, o.getValue().getType());
				Assert.assertEquals(123, o.getValue().getNumericValue().intValueExact());
			},
			(Observation o) ->  {
				Assert.assertEquals("T:type:dec", o.getConceptId());
				Assert.assertEquals(Value.Type.Numeric, o.getValue().getType());
				Assert.assertEquals(new BigDecimal("123.456"), o.getValue().getNumericValue());
				Assert.assertEquals("mm", o.getValue().getUnits());
			},
			(Observation o) ->  {
				Assert.assertEquals("T:full", o.getConceptId());
				Assert.assertEquals(Value.Type.Numeric, o.getValue().getType());
				Assert.assertEquals(new BigDecimal("123.456"), o.getValue().getNumericValue());
				Assert.assertEquals("mm", o.getValue().getUnits());
				
				Assert.assertEquals(ChronoUnit.YEARS, o.getStartTime().getAccuracy());
				Assert.assertEquals(2010, o.getStartTime().getLong(ChronoField.YEAR));

				Assert.assertEquals(ChronoUnit.YEARS, o.getEndTime().getAccuracy());
				Assert.assertEquals(2011, o.getEndTime().getLong(ChronoField.YEAR));

				Assert.assertEquals("T:LOC", o.getLocationId());
				// TODO test provider, flag
			},
			(Observation o) ->  {
				Assert.assertEquals("T:group:1", o.getConceptId());
				Assert.assertEquals(Value.Type.Text, o.getValue().getType());
				Assert.assertEquals("groupvalue", o.getValue().getStringValue());
				Assert.assertTrue(o.hasModifiers());
				Iterator<? extends Modifier> e = o.getModifiers();
				// TODO check modifier count
				Modifier m = o.getModifier("T:mod:1");
				Assert.assertNotNull(m);
				Assert.assertEquals("T:mod:1", m.getConceptId());
				Assert.assertNull(m.getValue());
				Assert.assertTrue(e.hasNext());
				m = o.getModifier("T:mod:2");
				Assert.assertNotNull(m);
				Assert.assertEquals(Value.Type.Text, m.getValue().getType());
				Assert.assertEquals("def456", m.getValue().getStringValue());
				Assert.assertTrue(e.hasNext());
				m = o.getModifier("T:mod:3");
				Assert.assertNotNull(m);
				Assert.assertEquals(Value.Type.Numeric, m.getValue().getType());
				Assert.assertEquals(new BigDecimal("78.9"), m.getValue().getNumericValue());
				
			},
			(Observation o) ->  {
				Assert.assertEquals("T:group:2", o.getConceptId());
				Assert.assertNull(o.getValue());
				Modifier m = o.getModifier("T:mod:1");
				Assert.assertNotNull(m);
				Assert.assertEquals("T:mod:1", m.getConceptId());
				Assert.assertNull(m.getValue());
			},
			
		});
	}
	
	public ObservationFactory getFactory(){
		return factory;
	}
	public ObservationSupplier getExampleSupplier() throws IOException{
		return getExampleSupplier("examples/dwh-jaxb.xml");
	}
	
	public ObservationSupplier getExampleSupplier(String path) throws IOException{
		try {
			return new JAXBObservationSupplier(factory, new FileInputStream(path));
		} catch (XMLStreamException | FactoryConfigurationError | JAXBException e) {
			throw new IOException(e);
		}
	}

	public void validateExample(Supplier<Observation> supplier){
		StreamSupport.stream(AbstractObservationParser.nonNullSpliterator(supplier), false).forEach(handler);		
	}
	
	@After
	public void closeHandler(){
		handler.finish();
	}
	
	@Test
	public void testStAXReader() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError  {
		XMLObservationSupplier xos = new XMLObservationSupplier(factory, new FileInputStream("examples/dwh-eav.xml"));
		validateExample(xos);
		xos.close();
	}
	
	@Test
	public void testJAXBReader() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError, JAXBException  {
		JAXBObservationSupplier xos = new JAXBObservationSupplier(factory, new FileInputStream("examples/dwh-jaxb.xml"));
		validateExample(xos);
		xos.close();
	}
	
	@Test
	public void testFlatReader() throws FileNotFoundException, IOException  {
		FlatObservationSupplier s = new FlatObservationSupplier(factory, new FileInputStream("examples/dwh-flat.txt"));
		validateExample(s);
		s.close();
	}
	
}
