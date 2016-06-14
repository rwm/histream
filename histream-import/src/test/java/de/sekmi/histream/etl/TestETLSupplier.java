package de.sekmi.histream.etl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.io.GroupedXMLWriter;
import de.sekmi.histream.io.Streams;

public class TestETLSupplier {
	private ETLObservationSupplier os;
	
	@Before
	public void loadConfiguration() throws IOException, ParseException{
		os = ETLObservationSupplier.load(getClass().getResource("/data/test-1-datasource.xml"));
	}

	@After
	public void freeResources() throws IOException{
		if( os != null )os.close();		
	}
	
	@Test
	public void validateRequiredFactAttributes() throws Exception{
		Observation fact = os.get();
		while( fact != null ){
			Assert.assertNotNull(fact.getPatientId());
			Assert.assertNotNull(fact.getStartTime());
			Assert.assertNotNull(fact.getConceptId());

			// source information
			ExternalSourceType source = fact.getSource();
			Assert.assertNotNull(source);
			Assert.assertNotNull(source.getSourceId());
			Assert.assertNotNull(source.getSourceTimestamp());

			// patient id should match patient.id
			Assert.assertEquals(fact.getPatientId(), fact.getExtension(Patient.class).getId());
			// encounter id should match encounter.id
			Assert.assertEquals(fact.getEncounterId(), fact.getExtension(Visit.class).getId());
			// next fact
			fact = os.get();
		}
	}
	
	@Test
	public void expectMetadataPresent() throws Exception{
		Assert.assertNotNull("Source id metadata required",os.getMeta(ObservationSupplier.META_SOURCE_ID));
		//Assert.assertNotNull("Source timestamp metadata required",os.getMeta(ObservationSupplier.META_SOURCE_TIMESTAMP));
		// verify all scripts are loaded
		ObservationFactory f = new ObservationFactoryImpl();
		FactGroupingQueue fq = os.getConfiguration().createFactQueue(f);
		Assert.assertTrue(fq instanceof ScriptProcessingQueue);
		ScriptProcessingQueue sq = (ScriptProcessingQueue)fq;
		Assert.assertEquals(2, sq.getNumScripts());
	}
	@Test
	public void testXMLConversion() throws Exception{
		GroupedXMLWriter w = new GroupedXMLWriter(System.out);
		// transfer meta information
		Meta.transfer(os, w);
		Streams.transfer(os, w);
		w.close();
	}

	
	@Test
	public void testReadFacts() throws IOException{
		while( true ){
			Observation fact = os.get();
			if( fact == null )break;
			
			StringBuilder debug_str = new StringBuilder();
			debug_str.append(fact.getPatientId()).append('\t');
			debug_str.append(fact.getEncounterId()).append('\t');
			debug_str.append(fact.getStartTime()).append('\t');
			debug_str.append(fact.getConceptId()).append('\t');
			debug_str.append(fact.getValue());
			
			System.out.println(debug_str.toString());
			// TODO test patient extension, visit extension
		}
	}
	
	@Test
	public void testPatientExtension() throws IOException{
		Observation fact = os.get();
		Assert.assertNotNull(fact);
		Patient p = fact.getExtension(Patient.class);
		Assert.assertNotNull(p);
		Assert.assertEquals("p1", p.getId());
		Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2003-02-01"), p.getBirthDate());
		Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2003-02-11"), p.getDeathDate());

		// TODO verify other patient information
		Assert.assertEquals("v1", p.getGivenName());
		Assert.assertEquals("n1", p.getSurname());
	}
	
	@Test
	public void testExtensionInstances() throws IOException{
		List<Observation> all = new ArrayList<>();
		os.stream().forEach(all::add);
		// nothing to do anymore
	}
	@Test
	public void testVisitExtension() throws IOException{
		Observation fact = os.get();
		Assert.assertNotNull(fact);

		Visit v = fact.getExtension(Visit.class);
		Assert.assertNotNull(v);

		Assert.assertEquals("v1", v.getId());
		// TODO make sure custom partial date format is parsed correctly for missing seconds
		//Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2013-03-20T09:00"), v.getStartTime());
		Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2013-03-21T13:00:21"), v.getEndTime());

		Assert.assertEquals("v1", v.getId());
		Assert.assertEquals(null, v.getLocationId());
	}
}
