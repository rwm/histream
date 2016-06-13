package de.sekmi.histream.etl;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.PostVisitFactCount.VisitCount;
import de.sekmi.histream.etl.config.DataSource;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.SimplePatientExtension;
import de.sekmi.histream.impl.SimpleVisitExtension;

public class TestVisitPostProcessorQueue {
	private ETLObservationSupplier os;
	private PostVisitFactCount v;
	
	@Before
	public void loadConfiguration() throws IOException, ParseException{
		DataSource ds = DataSource.load(getClass().getResource("/data/test-1-datasource.xml"));
		ObservationFactory factory = new ObservationFactoryImpl();
		factory.registerExtension(new SimplePatientExtension());
		factory.registerExtension(new SimpleVisitExtension());
		v = new PostVisitFactCount();
		os = new ETLObservationSupplier(ds, factory, v); 
	}

	@After
	public void freeResources() throws IOException{
		if( os != null )os.close();		
	}
	private void assertPatientVisitCount(List<VisitCount> actual, int index, String patient, String visit, int count){
		Assert.assertTrue(actual.size() > index);
		Assert.assertEquals(patient, actual.get(index).patid);
		Assert.assertEquals(visit, actual.get(index).visid);
		Assert.assertEquals(count, actual.get(index).count);
	}

	@Test
	public void verifyFactCount(){
		// process stream
		os.stream().count();
		// verify counts
		List<VisitCount> counts = v.getCounts();
		
		// print actual visit counts
//		for( VisitCount i : v.getCounts() ){
//			System.out.println(i.toString());
//		}
		
		assertPatientVisitCount(counts, 0, "p1", null, 0);
		assertPatientVisitCount(counts, 1, "p1", "v1", 13);
		assertPatientVisitCount(counts, 2, "p1", "v2", 12);
		assertPatientVisitCount(counts, 3, "p2", null, 0);
		assertPatientVisitCount(counts, 4, "p2", "v3", 12);
		assertPatientVisitCount(counts, 5, "p3", null, 0);
		assertPatientVisitCount(counts, 6, "p3", "v4", 13);
		Assert.assertEquals(7, counts.size());
	}
}