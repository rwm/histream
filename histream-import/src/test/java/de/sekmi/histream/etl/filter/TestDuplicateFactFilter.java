package de.sekmi.histream.etl.filter;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;

import javax.script.ScriptException;

import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.VisitPatientImpl;
import de.sekmi.histream.scripting.AbstractFacts;
import de.sekmi.histream.scripting.EncounterScriptEngine;
import de.sekmi.histream.scripting.Fact;

import static org.junit.Assert.*;

public class TestDuplicateFactFilter {

	// XXX implement filter first
	//@Test
	public void verifyComparator() throws ScriptException, ParseException{
		ObservationFactory of = new ObservationFactoryImpl();
		DuplicateFactFilter filter = new DuplicateFactFilter();
		EncounterScriptEngine e = new EncounterScriptEngine();
		e.setObservationFactory(of);
		PatientImpl p1=new PatientImpl("P1");
		VisitPatientImpl v1 = new VisitPatientImpl("E1", p1, new DateTimeAccuracy(Instant.now()));
		AbstractFacts facts = e.wrapEncounterFacts(v1,new ArrayList<>());
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
