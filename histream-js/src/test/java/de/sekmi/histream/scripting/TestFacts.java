package de.sekmi.histream.scripting;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.ObservationFactoryImpl;

public class TestFacts {

	ScriptEngineManager sem;
	ObservationFactory factory;
	ScriptEngine engine;
	List<Observation> list;
	DateTimeAccuracy defaultStart;
	
	@Before
	public void setup() throws ScriptException, ParseException{
		sem = new ScriptEngineManager();
		engine = sem.getEngineByName("nashorn");
		// enable strict mode
		engine.eval("'use strict';");
		factory = new ObservationFactoryImpl();
		
		defaultStart =  DateTimeAccuracy.parsePartialIso8601("2016");
		Observation[] facts = new Observation[]{
				factory.createObservation("P1", "C1", DateTimeAccuracy.parsePartialIso8601("2011")),
				factory.createObservation("P1", "C2", DateTimeAccuracy.parsePartialIso8601("2011-02-01")),
				factory.createObservation("P1", "C3", DateTimeAccuracy.parsePartialIso8601("2011-02-01"))				
		};
		this.list = new ArrayList<>();
		Collections.addAll(list, facts);
	}
	
	@Test
	public void verifyAddFacts() throws ScriptException{
		SimpleFacts f = new SimpleFacts(factory, "P1", "V1", defaultStart);
		f.setObservations(list);
		engine.put("facts", f);
		engine.eval("facts.add('C4')");

		// retrieve fact
		Fact t = f.get("C4");
		Assert.assertNotNull(t);
		Assert.assertEquals(defaultStart, t.getObservation().getStartTime());
	}
	
	
	@Test
	public void verifyHasFacts() throws ScriptException{
		SimpleFacts f = new SimpleFacts(factory, "P1", "V1", defaultStart);
		f.setObservations(list);
		engine.put("facts", f);

		Object ret;
		// verify logical and with two existing facts
		ret = engine.eval("Boolean(facts.get('C2') && facts.get('C3'))");
		Assert.assertTrue( ret instanceof Boolean );
		Assert.assertEquals(Boolean.TRUE, ret);

		// should return false as C4 does not exist 
		ret = engine.eval("Boolean(facts.get('C2') && facts.get('C4'))");
		Assert.assertTrue( ret instanceof Boolean );
		Assert.assertEquals(Boolean.FALSE, ret);
	}
}
