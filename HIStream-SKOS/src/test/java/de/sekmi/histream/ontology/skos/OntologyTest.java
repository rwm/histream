package de.sekmi.histream.ontology.skos;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.ontology.Concept;
import de.sekmi.histream.ontology.EnumValue;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.ValueRestriction;
import de.sekmi.histream.ontology.skos.Store;

public class OntologyTest {
	Store store;
	
	@Before
	public void setupOntology()throws Exception{
		store = new Store(new File("src/main/examples/test-ontology.ttl"));
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
		// German language
		ValueRestriction rest = c.getValueRestriction();
		Assert.assertNotNull(rest);
		EnumValue[] e = rest.getEnumeration(Locale.GERMAN);
		Assert.assertNotNull(e);
		Assert.assertEquals(2, e.length);
		Assert.assertEquals("1", e[0].getValue());
		Assert.assertEquals("1_de", e[0].getPrefLabel());
		Assert.assertEquals("2", e[1].getValue());
		Assert.assertEquals("2_de", e[1].getPrefLabel());
		// German language
		e = rest.getEnumeration(Locale.ENGLISH);
		Assert.assertNotNull(e);
		Assert.assertEquals(2, e.length);
		Assert.assertEquals("1", e[0].getValue());
		Assert.assertEquals("1_en", e[0].getPrefLabel());
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
		try( Store store = new Store(new File("src/main/examples/test-ontology.n3")) ){
			store.printConceptHierarchy();
			
			Concept c = store.getConceptByNotation("Type");
			System.out.println("Concept:"+c);
		}
		
	}

}
