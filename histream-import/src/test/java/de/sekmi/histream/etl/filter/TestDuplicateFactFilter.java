package de.sekmi.histream.etl.filter;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;

import javax.script.ScriptException;

import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.SimplePatientExtension;
import de.sekmi.histream.impl.SimpleVisitExtension;
import de.sekmi.histream.scripting.AbstractFacts;
import de.sekmi.histream.scripting.EncounterScriptEngine;
import de.sekmi.histream.scripting.Fact;

import static org.junit.Assert.*;

public class TestDuplicateFactFilter {

	@Test
	public void verifyComparator() throws ScriptException, ParseException{
		ObservationFactory of = new ObservationFactoryImpl(new SimplePatientExtension(), new SimpleVisitExtension());
		DuplicateFactFilter filter = new DuplicateFactFilter();
		EncounterScriptEngine e = new EncounterScriptEngine();
		e.setObservationFactory(of);
		AbstractFacts facts = e.wrapEncounterFacts("P1", "E1", DateTimeAccuracy.parsePartialIso8601("2001-02-03", ZoneId.systemDefault()), new ArrayList<>());
		Fact a = facts.add("lala");
		Fact b = facts.add("xx");
		assertTrue( DuplicateFactFilter.compare(a, b) < 0 );
		facts.add("lala");
		filter.processVisit(facts);
		assertEquals(2, facts.size());
		// TODO compare with timestamps
	}
}
