package de.sekmi.histream.impl;

import java.util.function.Consumer;

import org.junit.Assert;

import de.sekmi.histream.Observation;

public class TestObservationHandler implements Consumer<Observation>{

	public static interface Tester{
		void test(Observation observation);
	}
	
	private Tester[] tests;
	private int count;
	
	public TestObservationHandler(Tester[] tests){
		this.tests = tests;
	}
	
	@Override
	public void accept(Observation observation) {
		Assert.assertTrue("Not enough tests. Next concept "+observation.getConceptId(), count < tests.length);
		//Assert.assertTrue(
		//		"Observation["+count+"] concept '"+observation.getConceptId(), 
				tests[count].test(observation);
		//);
		count ++;
	}
	
	public void finish(){
		Assert.assertEquals(tests.length, count);
	}

}
