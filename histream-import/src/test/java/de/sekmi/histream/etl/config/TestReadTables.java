package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.junit.Test;

import de.sekmi.histream.etl.PatientRow;
import de.sekmi.histream.etl.PatientStream;
import org.junit.Assert;

public class TestReadTables {

	@Test
	public void testReadPatients() throws IOException{
		DataSource ds;
		try( InputStream in = getClass().getResourceAsStream("/test-1-datasource.xml") ){
			ds = JAXB.unmarshal(in, DataSource.class);
		}
		try( PatientStream s = ds.patientTable.open() ){
			PatientRow r = s.get();
			Assert.assertEquals("1", r.getId());
			System.out.println(r.getBirthDate());
			
		}
	}
}
