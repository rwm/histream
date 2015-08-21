package de.sekmi.histream.etl;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.config.DataSource;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.SimplePatientExtension;
import de.sekmi.histream.impl.SimpleVisitExtension;

public class TestETLSupplier {
	private DataSource ds;
	private ObservationFactory of ;
	private ETLObservationSupplier os;
	
	@Before
	public void loadConfiguration() throws IOException, ParseException{
		try( InputStream in = getClass().getResourceAsStream("/test-1-datasource.xml") ){
			ds = JAXB.unmarshal(in, DataSource.class);
		}
		of = new ObservationFactoryImpl();
		of.registerExtension(new SimplePatientExtension());
		of.registerExtension(new SimpleVisitExtension());
		os = new ETLObservationSupplier(ds,of);
	}

	@After
	public void freeResources() throws IOException{
		os.close();		
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
		// TODO verify other patient information
	}

}
