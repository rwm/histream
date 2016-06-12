package de.sekmi.histream.scripting;

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
	@Before
	public void setup(){
		sem = new ScriptEngineManager();
		engine = sem.getEngineByName("nashorn");
		factory = new ObservationFactoryImpl();
		
	}
	
	@Test
	public void verifyAddFacts() throws ScriptException{
		final DateTimeAccuracy defaultStart =  DateTimeAccuracy.parsePartialIso8601("2016");
		Observation[] facts = new Observation[]{
				factory.createObservation("P1", "C1", DateTimeAccuracy.parsePartialIso8601("2011")),
				factory.createObservation("P1", "C2", DateTimeAccuracy.parsePartialIso8601("2011-02-01")),
				factory.createObservation("P1", "C3", DateTimeAccuracy.parsePartialIso8601("2011-02-01"))				
		};
		Facts f = new Facts(factory, "P1", "V1", defaultStart);
		List<Observation> list = new ArrayList<>();
		Collections.addAll(list, facts);
		
		f.setObservations(list);
		engine.put("facts", f);
		engine.eval("facts.add('C4')");
		
		// retrieve fact
		Fact t = f.get("C4");
		Assert.assertNotNull(t);
		Assert.assertEquals(defaultStart, t.getObservation().getStartTime());
		
		
	}
}
