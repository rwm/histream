package de.sekmi.histream.etl;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.config.DataSource;
import de.sekmi.histream.impl.ObservationFactoryImpl;

public class TestETLSupplier {
	private DataSource ds;
	private ObservationFactory of;
	
	@Before
	public void loadConfiguration() throws IOException{
		try( InputStream in = getClass().getResourceAsStream("/test-1-datasource.xml") ){
			ds = JAXB.unmarshal(in, DataSource.class);
		}
		of = new ObservationFactoryImpl();
	}

	@Test
	public void testReadFacts() throws IOException, ParseException{
		ETLObservationSupplier s = new ETLObservationSupplier(ds,of);
		while( true ){
			Observation fact = s.get();
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
		s.close();
	}
}
