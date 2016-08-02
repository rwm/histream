package de.sekmi.histream.export.config;

import java.util.ArrayList;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Assert;
import org.junit.Test;

public class TestMarshal {

	private ExportDescriptor createDescriptor(){
		ExportDescriptor e = new ExportDescriptor();
		e.concepts = new Concepts();
		e.concepts.concepts = new ArrayList<>();
		e.concepts.concepts.add(Concept.newNotation("ABC"));
		e.concepts.concepts.add(Concept.newWildcard("CEDIS:*"));

		e.concepts.groups = new ArrayList<>();
		ConceptGroup d = new ConceptGroup("diag");
		d.concepts.add(Concept.newWildcard("ICD10:*"));
		e.concepts.groups.add(d);
		e.patient = new PatientTable();
		e.patient.columns = new Column[2];
		e.patient.columns[0] = new Column("pid","@id");
		e.patient.columns[1] = new Column("dob","birthdate");

		return e;
	}
	@Test
	public void verifyMarshal() throws JAXBException{
		ExportDescriptor e = createDescriptor();
		JAXBContext j = JAXBContext.newInstance(ExportDescriptor.class);
		Marshaller m = j.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(e, System.out);
	}
	
	@Test
	public void verifyIterableIterable(){
		ExportDescriptor e = createDescriptor();
		long count = StreamSupport.stream(e.concepts.allConcepts().spliterator(), false).count();
		Assert.assertEquals(3, count);
	}
	
	@Test
	public void verifyUnmarshall(){
		ExportDescriptor e;
		// first example
		e = JAXB.unmarshal(getClass().getResourceAsStream("/export1.xml"), ExportDescriptor.class);
		Assert.assertEquals(4, StreamSupport.stream(e.concepts.allConcepts().spliterator(), false).count());
		// second example
		e = JAXB.unmarshal(getClass().getResourceAsStream("/export2.xml"), ExportDescriptor.class);
		Assert.assertEquals(5, StreamSupport.stream(e.concepts.allConcepts().spliterator(), false).count());
	}
}
