package de.sekmi.histream.etl;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.io.GroupedXMLWriter;
import de.sekmi.histream.io.Streams;

public class TestETLSupplier {
	private ETLObservationSupplier os;
	private ZoneId zone;
	
	@Before
	public void loadConfiguration() throws IOException, ParseException{
		os = ETLObservationSupplier.load(getClass().getResource("/data/test-1-datasource.xml"));
		this.zone = os.getConfiguration().getMeta().getTimezone();
		Assert.assertNotNull(zone);
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
			Assert.assertEquals(fact.getPatientId(), fact.getVisit().getPatient().getId());
			// encounter id should match encounter.id
			Assert.assertEquals(fact.getEncounterId(), fact.getVisit().getId());
			// next fact
			fact = os.get();
		}
	}
	
	@Test
	public void expectMetadataPresent() throws Exception{
		Assert.assertNotNull("Source id metadata required",os.getMeta(Meta.META_SOURCE_ID,null));
		//Assert.assertNotNull("Source timestamp metadata required",os.getMeta(ObservationSupplier.META_SOURCE_TIMESTAMP));
		// verify all scripts are loaded
//		ObservationFactory f = new ObservationFactoryImpl();
//		FactGroupingQueue fq = os.getConfiguration().createFactQueue(f);
//		Assert.assertTrue(fq instanceof ScriptProcessingQueue);
//		ScriptProcessingQueue sq = (ScriptProcessingQueue)fq;
//		Assert.assertEquals(2, sq.getNumScripts());
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
	public void testExtensionInstances() throws IOException{
		List<Observation> all = new ArrayList<>();
		os.stream().forEach(all::add);
		// nothing to do anymore
	}
	@Test
	public void missingStartTimestampUsesVisitTimestamp() throws IOException, java.text.ParseException{
		List<Observation> all = new ArrayList<>();
		os.stream().filter( o -> o.getConceptId().equals("natrium") ).forEach(all::add);
		for( Observation o : all ){
			System.out.println("Natrium-start: "+o.getStartTime());
		}
	}
	@Test
	public void verifyInlineScriptExecution() throws IOException, java.text.ParseException{
		List<Observation> all = new ArrayList<>();
		
		os.stream().filter( o -> o.getConceptId().equals("cnt") ).forEach(all::add);
//		for( Observation o : all ){
//			System.out.println("cnt: "+o.getStartTime()+", "+o.getValue().getStringValue());
//		}
		// should have a cnt fact for each visit
		Assert.assertEquals(4, all.size());
	}
	@Test
	public void verifyExternalScriptExecution() throws IOException, java.text.ParseException{
		List<Observation> all = new ArrayList<>();
		
		os.stream().filter( o -> o.getConceptId().equals("ext-js") ).forEach(all::add);
//		for( Observation o : all ){
//			System.out.println("cnt: "+o.getStartTime()+", "+o.getValue().getStringValue());
//		}
		// should have a cnt fact for each visit
		Assert.assertEquals(4, all.size());
	}

}
