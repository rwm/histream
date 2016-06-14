package de.sekmi.histream.scripting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.impl.CachedPatientExtension;
import de.sekmi.histream.impl.ExternalSourceImpl;
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
		e.addScript(getClass().getResource("/postprocess-encounter-1.js"), "UTF-8", "script1");
		// run
		e.processEncounter("P1", "E1", DateTimeAccuracy.parsePartialIso8601("2011-02-01"), list);

		// verify
		Observation last = list.get(list.size()-1);
		Assert.assertEquals("test1", last.getConceptId());
		Assert.assertEquals(Value.Type.Numeric, last.getValue().getType());
	}
	
	@Test
	/**
	 * Verifies that the non-extension script call for {@link AbstractFacts}
	 * will work with extensions (if the patient extension is cached)
	 * 
	 * @throws Exception failure
	 */
	public void verifyPatientExtensionForNonExtensionCall() throws Exception{
		ObservationFactory factory = new ObservationFactoryImpl();
		// this test works only with CachedPatientExtension
		factory.registerExtension(new CachedPatientExtension());
		DateTimeAccuracy start = DateTimeAccuracy.parsePartialIso8601("2011");
		// create full observation
		Observation o = factory.createObservation("P1", "C1", start);
		o.setSource(new ExternalSourceImpl());
		Patient p = o.getExtension(Patient.class);
		p.setGivenName("A");
		p.setSurname("B");
		p.setBirthDate(start);
		// create list
		List<Observation> list = new ArrayList<>();
		list.add(o);
		
		// prepare engine
		EncounterScriptEngine e = new EncounterScriptEngine();
		e.setObservationFactory(factory);
		e.addScript(getClass().getResource("/postprocess-encounter-1.js"), "UTF-8", "script1");
		// run
		e.processEncounter("P1", "C1", DateTimeAccuracy.parsePartialIso8601("2011-02-01"), list);
		
		Observation o2 = list.get(1);
		Assert.assertNotEquals(o, o2);
		Patient p2 = o2.getExtension(Patient.class);
		Assert.assertNotNull(p2);
		Assert.assertEquals(p, p2);
		
	}
}
