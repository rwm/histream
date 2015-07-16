package de.sekmi.histream.ontology.skos.transform;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.io.FileObservationProviderTest;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.skos.Store;
import de.sekmi.histream.ontology.skos.transform.Rule;
import de.sekmi.histream.ontology.skos.transform.TransformationRules;

public class TransformationRuleTest {
	private Store store;
	public static final String TEST_PREFIX="http://sekmi.de/histream/examples/mapping#";
	
	@Before
	public void setupOntology()throws Exception{
		store = new Store(new File("examples/test-mapping.ttl"));
	}

	@Test
	public void testGetRules() throws OntologyException{
		TransformationRules r = store.getConceptTransformations("source1", TEST_PREFIX+"source");
		Assert.assertNotNull(r);
		Assert.assertNotNull(r.getMapRules());		
		Assert.assertEquals(1, r.getMapRules().length);
		Rule rule = r.getMapRules()[0];
		Assert.assertNotNull(rule.condition);
		Assert.assertNotNull(rule.getTarget());
		Assert.assertEquals(ConditionType.XPath, rule.conditionType);
	}
	
	// TODO perform transformation
	@Test
	public void testApplyRules() throws Exception{
		TransformationRules r = store.getConceptTransformations("source1", TEST_PREFIX+"source");
		Assert.assertNotNull(r);
		
		RDFTransformation t = new RDFTransformation(null, store, TEST_PREFIX+"source", true);
		FileObservationProviderTest p = new FileObservationProviderTest();
		p.initializeObservationFactory();
		
		ObservationSupplier s = p.getExampleSupplier("examples/test-mapping-facts.xml");
		
		Observation o, o2;

		o = s.get();
		Assert.assertEquals("source1", o.getConceptId());
		o2 = t.transform(o, null);
		Assert.assertEquals("T:t1", o2.getConceptId());
		
		o = s.get(); // no match (value != 1)
		Assert.assertEquals("source1", o.getConceptId());
		o2 = t.transform(o, null);
		Assert.assertEquals("source1", o2.getConceptId());

		o = s.get();
		Assert.assertEquals("source2", o.getConceptId());
		o2 = t.transform(o, null);
		Assert.assertEquals("T:t2", o2.getConceptId());
		
		o = s.get();
		Assert.assertEquals("source3", o.getConceptId());
		o2 = t.transform(o, null);
		Assert.assertEquals("T:t1", o2.getConceptId());
		
		o = s.get();
		Assert.assertEquals("source3", o.getConceptId());
		o2 = t.transform(o, null);
		Assert.assertEquals("T:t2", o2.getConceptId());
		
		// TODO implement choose+otherwise
		/*
		o = s.get();
		Assert.assertEquals("source3", o.getConceptId());
		o2 = t.transform(o, null);
		Assert.assertEquals("T:t3", o2.getConceptId());
		*/
		
		
		s.close();
	}
	
}
