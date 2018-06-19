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

	// XXX implement filter first
	//@Test
	public void verifyComparator() throws ScriptException, ParseException{
		ObservationFactory of = new ObservationFactoryImpl(new SimplePatientExtension(), new SimpleVisitExtension());
		DuplicateFactFilter filter = new DuplicateFactFilter();
		EncounterScriptEngine e = new EncounterScriptEngine();
		e.setObservationFactory(of);
		AbstractFacts facts = e.wrapEncounterFacts("P1", "E1", DateTimeAccuracy.parsePartialIso8601("2001-02-03", ZoneId.systemDefault()), new ArrayList<>());
		Fact a = facts.add("lala");
		Fact b = facts.add("xx");
//		assertTrue( DuplicateFactFilter.compare(a, b) < 0 );
		b.start("2000-01-02T03:04:05Z");
//		assertTrue( DuplicateFactFilter.compare(a, b) > 0 );
		Fact c = facts.add("xx").start("2000-01-02T03:05Z");
		Fact d = facts.add("lala"); // add duplicate
//		assertTrue( DuplicateFactFilter.compare(c, b) > 0 );
//		assertTrue( DuplicateFactFilter.compare(a, d) == 0 );
//		assertTrue( DuplicateFactFilter.compare(a, c) > 0 );
//		assertTrue( DuplicateFactFilter.compare(d, c) > 0 );
		
		filter.processVisit(facts);
		assertEquals(3, facts.size());
		// TODO compare with timestamps
	}
}
