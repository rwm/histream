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


import java.util.function.Consumer;

import org.junit.Assert;

import de.sekmi.histream.Observation;

public class TestObservationHandler implements Consumer<Observation>{

	public static interface Tester{
		void test(Observation observation);
	}
	
	private Tester[] tests;
	private int count;
	
	public TestObservationHandler(Tester... tests){
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
