package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.temporal.ChronoField;

import javax.xml.bind.JAXB;

import org.junit.Test;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.PatientRow;
import de.sekmi.histream.etl.RecordSupplier;
import de.sekmi.histream.etl.VisitRow;
import de.sekmi.histream.etl.WideRow;
import de.sekmi.histream.impl.ObservationFactoryImpl;

import org.junit.Assert;
import org.junit.Before;

public class TestReadTables {
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
	public void testReadPatients() throws IOException, ParseException{
		try( RecordSupplier<PatientRow> s = ds.patientTable.open(of) ){
			PatientRow r = s.get();
			Assert.assertEquals("p1", r.getId());
			Assert.assertEquals(2003, r.getBirthDate().get(ChronoField.YEAR));
			
		}
	}
	@Test
	public void testReadVisits() throws IOException, ParseException{
		try( RecordSupplier<VisitRow> s = ds.visitTable.open(of) ){
			VisitRow r = s.get();
			Assert.assertEquals("1", r.getId());
			Assert.assertEquals(2013, r.getStartTime().get(ChronoField.YEAR));
			
		}
	}
	@Test
	public void testReadWideTable() throws IOException, ParseException{
		try( RecordSupplier<WideRow> s = ds.wideTables[0].open(of) ){
			WideRow r = s.get();
			Assert.assertNotNull(r);
			Assert.assertTrue(r.getFacts().size() > 0);
			Observation o = r.getFacts().get(0);
			Assert.assertEquals("natrium", o.getConceptId());
			Assert.assertEquals(Value.Type.Numeric, o.getValue().getType());
			Assert.assertEquals(BigDecimal.valueOf(124), o.getValue().getNumericValue());
		}
	}
}
