package de.sekmi.histream.ontology.skos;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.ontology.Concept;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.ValueRestriction;
import de.sekmi.histream.ontology.skos.Store;

public class OntologyTest {
	Store store;
	final static String NS_PREFIX="http://sekmi.de/histream/skos/tests#";
	
	@Before
	public void setupOntology()throws Exception{
		store = new Store(new File("examples/test-ontology.ttl"));
		store.setConceptScheme(NS_PREFIX+"TestScheme");
	}

	@Test
	public void getConceptByIdTest() throws OntologyException{
		Assert.assertNull(store.getConceptByNotation("notfound"));
		Concept c = store.getConceptByNotation("T:type:int");
		Assert.assertNotNull(c);
		Assert.assertEquals(1, c.getIDs().length);
		Assert.assertEquals("T:type:int", c.getIDs()[0]);
		ValueRestriction rest = c.getValueRestriction();
		Assert.assertNotNull(rest);
	}

	@Test
	public void enumRestrictionTest() throws OntologyException{
		Concept c = store.getConceptByNotation("T:Enum");
		Assert.assertNotNull(c);
		// Values
		ValueRestriction rest = c.getValueRestriction();
		Assert.assertNotNull(rest);
		Object[] values = rest.getEnumerationValues();
		Assert.assertEquals("1", values[0]);
		Assert.assertEquals("2", values[1]);

		// German language
		String[] labels = rest.getEnumerationLabels(Locale.GERMAN);
		Assert.assertNotNull(labels);
		Assert.assertEquals(2, labels.length);
		Assert.assertEquals("1_de", labels[0]);
		Assert.assertEquals("2_de", labels[1]);
		
		// English language
		labels = rest.getEnumerationLabels(Locale.ENGLISH);
		Assert.assertNotNull(labels);
		Assert.assertEquals(2, labels.length);
		Assert.assertEquals("1_en", labels[0]);
		Assert.assertEquals("2_en", labels[1]);
	}


	@Test
	public void getNarrowerTest() throws OntologyException{
		Concept[] top = store.getTopConcepts();
		Assert.assertNotNull(top);
		Assert.assertEquals(1, top.length);
		
		Concept[] narrower = top[0].getNarrower();
		Assert.assertNotNull(narrower);	
		Assert.assertEquals(4, narrower.length);
	}
	
	@Test
	public void inferredBroaderTest() throws OntologyException{
		Concept[] top = store.getTopConcepts();
		Assert.assertNotNull(top);
		Assert.assertEquals(1, top.length);
		
		Concept[] narrower = top[0].getNarrower();
		Assert.assertNotNull(narrower);	
		for( int i=0; i<narrower.length; i++ ){
			Concept[] broader = narrower[i].getBroader();
			Assert.assertNotNull(broader);
			Assert.assertEquals(1, broader.length);
			Assert.assertEquals(top[0], broader[0]);		
		}
	}
	
	@Test
	public void inferredHasTopConceptAndNarrowerTest() throws OntologyException{
		Concept[] top = store.getTopConcepts(NS_PREFIX+"OtherScheme");
		Assert.assertNotNull(top);
		Assert.assertEquals(1, top.length);
		
		Concept[] narrower = top[0].getNarrower();
		Assert.assertNotNull(narrower);
		Assert.assertEquals(1, narrower.length);
		String[] ids = narrower[0].getIDs();
		Assert.assertEquals(1, ids.length);
		Assert.assertEquals("other", ids[0]);
	}
	
	@Test
	public void testLanguage() throws OntologyException{
		Concept c = store.getConceptByNotation("T:type:int");
		Assert.assertNotNull(c);
		Assert.assertEquals("Integer_label_de", c.getPrefLabel(Locale.GERMAN));
		Assert.assertEquals("Integer_label_en", c.getPrefLabel(Locale.ENGLISH));
		Assert.assertNull(c.getPrefLabel(Locale.JAPANESE));
		Assert.assertEquals("Description_neutral", c.getDescription(Locale.ROOT));
		Assert.assertEquals("Description_de", c.getDescription(Locale.GERMAN));
	}
	
	@After
	public void closeOntology() throws IOException{
		store.close();
	}
	
	public static void main(String[] args) throws Exception{
		try( Store store = new Store(new File("examples/test-ontology.ttl")) ){
			store.printConceptHierarchy();
			
			Concept c = store.getConceptByNotation("Type");
			System.out.println("Concept:"+c);
		}
		
	}

}
