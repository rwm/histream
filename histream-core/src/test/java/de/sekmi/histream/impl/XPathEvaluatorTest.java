package de.sekmi.histream.impl;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.io.FileObservationProviderTest;

public class XPathEvaluatorTest {

	private XPathEvaluator eval;
	
	@Before
	public void initialize() throws JAXBException{
		eval = new XPathEvaluator();
	}
	
	@Test 
	public void testExpressions() throws Exception{
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		ObservationSupplier s = t.getExampleSupplier();
		Observation o = s.get();
		//System.out.println("XXX:"+eval.evaluateToString("translate(fact/@start,'-:T','')", o));
		//System.out.println(XPathEvaluator.toXMLString(o));
		String[] trueExpressions = new String[]{"fact/@patient", "fact/@concept", "fact/@start", "number(translate(fact/@start,'-:T',''))=201409070104003"};
		for( String expr : trueExpressions ){
			Assert.assertEquals(expr, true, eval.test(expr, o));
		}
		// skip to string value
		for( int i=0; i<6; i++ ){
			o = s.get();
		}

		// compare string value
		Assert.assertEquals(true, eval.test("fact/value='abc123'", o));
		
		// compare numeric value
		o = s.get();
		Assert.assertEquals(true, eval.test("fact/value > 122 and fact/value < 124", o));

		// compare value with unit 
		// XXX flag not supported yet
		o = s.get();
		o = s.get();
		Assert.assertEquals(true, eval.test("fact/value/@unit = 'mm' and fact/value > 123", o));

		// compare modifier values
		o = s.get();
		Assert.assertEquals(true, eval.test("fact/modifier[@code='T:mod:3']/value = 78.9 and fact/modifier[@code='T:mod:1']", o));
		
		s.close();
	}
	

}
