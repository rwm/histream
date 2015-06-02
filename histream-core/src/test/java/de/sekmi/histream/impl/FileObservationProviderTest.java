package de.sekmi.histream.impl;

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
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.stream.StreamSupport;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.Value;
import de.sekmi.histream.Modifier;
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
				Assert.assertEquals("abc123", o.getValue().getValue());
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
				Assert.assertEquals("groupvalue", o.getValue().getValue());
				Assert.assertTrue(o.hasModifiers());
				Enumeration<Modifier> e = o.getModifiers();
				// TODO check modifier count
				Modifier m = o.getModifier("T:mod:1");
				Assert.assertNotNull(m);
				Assert.assertEquals("T:mod:1", m.getConceptId());
				Assert.assertEquals(Value.Type.None, m.getValue().getType());
				Assert.assertTrue(e.hasMoreElements());
				m = o.getModifier("T:mod:2");
				Assert.assertNotNull(m);
				Assert.assertEquals(Value.Type.Text, m.getValue().getType());
				Assert.assertEquals("def456", m.getValue().getValue());
				Assert.assertTrue(e.hasMoreElements());
				m = o.getModifier("T:mod:3");
				Assert.assertNotNull(m);
				Assert.assertEquals(Value.Type.Numeric, m.getValue().getType());
				Assert.assertEquals(new BigDecimal("78.9"), m.getValue().getNumericValue());
				
			},
			
		});
	}
	
	@Test
	public void testStAXReader() throws Exception {
		ObservationSupplier xos = new XMLObservationSupplier(factory, new FileInputStream("src/main/examples/dwh-eav.xml"));
		StreamSupport.stream(AbstractObservationParser.nonNullSpliterator(xos), false).forEach(handler);
		handler.finish();
	}
	
	@Test
	public void testFlatReader() throws Exception {
		ObservationSupplier s = new FlatObservationSupplier(factory, new FileInputStream("src/main/examples/dwh-flat.txt"));
		StreamSupport.stream(AbstractObservationParser.nonNullSpliterator(s), false).forEach(handler);
		handler.finish();
	}
	
}
