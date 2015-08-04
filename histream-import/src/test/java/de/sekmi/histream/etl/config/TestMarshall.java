package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXB;

import org.junit.Assert;

import org.junit.Test;

import de.sekmi.histream.etl.config.DataSource;

public class TestMarshall {

	@Test
	public void testUnmarshall() throws IOException{
		try( InputStream in = getClass().getResourceAsStream("/test-1-datasource.xml") ){
			DataSource ds = JAXB.unmarshal(in, DataSource.class);
			Assert.assertNotNull(ds.meta);
			Assert.assertEquals("replace-source",ds.meta.etlStrategy);
			Assert.assertEquals("test-1",ds.meta.sourceId);
			// patient table
			Assert.assertNotNull(ds.patientTable);
			Assert.assertNotNull(ds.patientTable.source);
			Assert.assertEquals("\\t", ((FileSource)ds.patientTable.source).separator);
			Assert.assertNotNull(ds.patientTable.idat);
			Assert.assertEquals("patid",ds.patientTable.idat.patientId.name);
			Assert.assertEquals("geburtsdatum",ds.patientTable.idat.birthdate.name);
			Assert.assertEquals("geschlecht",ds.patientTable.idat.gender.name);
			// visit table
			Assert.assertNotNull(ds.visitTable);
			Assert.assertNotNull(ds.visitTable.source);
			Assert.assertNotNull(ds.visitTable.idat);
			Assert.assertEquals("patid",ds.visitTable.idat.patientId.name);
			Assert.assertEquals("fallnr",ds.visitTable.idat.visitId.name);
			// wide table
			Assert.assertNotNull(ds.wideTables);
			Assert.assertEquals(1, ds.wideTables.length);
			WideTable t = ds.wideTables[0];
			Assert.assertNotNull(t);
			Assert.assertNotNull(t.idat);
			Assert.assertEquals("patid",t.idat.patientId.name);
			Assert.assertEquals("fallnr",t.idat.visitId.name);
			// concepts
			Assert.assertNotNull(t.concepts);
			Assert.assertTrue(t.concepts.length > 0);
			Concept c = t.concepts[0];
			Assert.assertNotNull(c);
			Assert.assertEquals("natrium", c.id);
			Assert.assertEquals("na", c.value.name);
			Assert.assertEquals("mmol/l", c.unit.constantValue);
		}
	}
	@Test
	public void testMarshal() throws MalformedURLException{
		DataSource s = new DataSource();
		s.meta = new Meta();
		s.meta.sourceId = "SID";
		s.meta.etlStrategy = "replace-source";
		s.xmlSources = new XmlSource[1];
		s.xmlSources[0] = new XmlSource();
		s.xmlSources[0].url = new URL("http://lala");
		s.xmlSources[0].transform = new XmlSource.Transform[1];
		s.xmlSources[0].transform[0] = new XmlSource.Transform("file:my.xsl","c:/to/file");
		s.patientTable = new PatientTable();
		FileSource fs = new FileSource("file:patient.source","text/csv");
		fs.separator = "\\t";
		s.patientTable.source = fs;
		s.patientTable.idat = new PatientTable.IDAT();
		s.patientTable.idat.patientId = new StringColumn("patid"); 
		s.visitTable = new VisitTable();
		s.visitTable.source = new FileSource("file:lala.txt", "text/plain");
		s.visitTable.idat = new VisitTable.IDAT();
		s.visitTable.idat.patientId = new StringColumn("patid");
		s.visitTable.idat.visitId = new StringColumn("visit");		
		s.visitTable.concepts = new Concept[1];
		s.visitTable.concepts[0] = new Concept("vconcept","start","yyyy-MM-ddTHH:mm:ss");
		s.wideTables = new WideTable[1];
		s.wideTables[0] = new WideTable();
		s.wideTables[0].source = new SQLSource("org.postgresql.Driver","jdbc:postgresql://localhost:15432/i2b2");
		s.wideTables[0].idat = new DataTableIdat();
		s.wideTables[0].idat.patientId = new StringColumn("patid");
		s.wideTables[0].concepts = new Concept[2];
		s.wideTables[0].concepts[0] = new Concept("ACC","zeit","yyyy-MM-ddTHH:mm:ss");
		s.wideTables[0].concepts[0].modifiers = new Concept.Modifier[1];
		s.wideTables[0].concepts[0].modifiers[0] = new Concept.Modifier("DOSE");
		s.wideTables[0].concepts[0].modifiers[0].value = new StringColumn("dosis");

		JAXB.marshal(s, System.out);

	}
}