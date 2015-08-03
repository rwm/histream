package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.temporal.ChronoField;

import javax.xml.bind.JAXB;

import org.junit.Test;

import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.PatientRow;
import de.sekmi.histream.etl.RecordSupplier;
import de.sekmi.histream.etl.VisitRow;

import org.junit.Assert;

public class TestReadTables {

	@Test
	public void testReadPatients() throws IOException, ParseException{
		DataSource ds;
		try( InputStream in = getClass().getResourceAsStream("/test-1-datasource.xml") ){
			ds = JAXB.unmarshal(in, DataSource.class);
		}
		try( RecordSupplier<PatientRow> s = ds.patientTable.open() ){
			PatientRow r = s.get();
			Assert.assertEquals("1", r.getId());
			Assert.assertEquals(2003, r.getBirthDate().get(ChronoField.YEAR));
			
		}
	}
	@Test
	public void testReadVisits() throws IOException, ParseException{
		DataSource ds;
		try( InputStream in = getClass().getResourceAsStream("/test-1-datasource.xml") ){
			ds = JAXB.unmarshal(in, DataSource.class);
		}
		try( RecordSupplier<VisitRow> s = ds.visitTable.open() ){
			VisitRow r = s.get();
			Assert.assertEquals("1", r.getId());
			Assert.assertEquals(2013, r.getStartTime().get(ChronoField.YEAR));
			
		}
	}
}
