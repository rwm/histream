package de.sekmi.histream.ontology.skos.transform;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
	}
	
}
