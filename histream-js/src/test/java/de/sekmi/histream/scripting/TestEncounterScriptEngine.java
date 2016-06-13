package de.sekmi.histream.scripting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.impl.ObservationFactoryImpl;

public class TestEncounterScriptEngine {

	@Test
	public void multipleCalls() throws Exception{
		// prepare facts
		ObservationFactory factory = new ObservationFactoryImpl();
		Observation[] facts = new Observation[]{
				factory.createObservation("P1", "C1", DateTimeAccuracy.parsePartialIso8601("2011")),
				factory.createObservation("P1", "C2", DateTimeAccuracy.parsePartialIso8601("2011-02-01")),
				factory.createObservation("P1", "C3", DateTimeAccuracy.parsePartialIso8601("2011-02-01"))				
		};
		List<Observation> list = new ArrayList<>();
		Collections.addAll(list, facts);

		// prepare engine
		EncounterScriptEngine e = new EncounterScriptEngine();
		e.setObservationFactory(factory);
		e.addScript(getClass().getResource("/postprocess-encounter-1.js"), "UTF-8");

		// run
		e.processEncounter("P1", "E1", DateTimeAccuracy.parsePartialIso8601("2011-02-01"), list);

		// verify
		Observation last = list.get(list.size()-1);
		Assert.assertEquals("test1", last.getConceptId());
		Assert.assertEquals(Value.Type.Numeric, last.getValue().getType());
	}
}
