package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.temporal.ChronoField;

import javax.xml.bind.JAXB;

import org.junit.Test;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.etl.EavRow;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.PatientRow;
import de.sekmi.histream.etl.RecordSupplier;
import de.sekmi.histream.etl.VisitRow;
import de.sekmi.histream.etl.WideRow;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.ObservationFactoryImpl;

import org.junit.Assert;
import org.junit.Before;

public class TestReadTables {
	private DataSource ds;
	private ObservationFactory of;
	
	@Before
	public void loadConfiguration() throws IOException{
		URL url = getClass().getResource("/data/test-1-datasource.xml");
		ds = JAXB.unmarshal(url, DataSource.class);
		ds.getMeta().setLocation(url);
		of = new ObservationFactoryImpl();
	}
	
	@Test
	public void testReadPatients() throws IOException, ParseException{
		try( RecordSupplier<PatientRow> s = ds.patientTable.open(of,ds.getMeta()) ){
			PatientRow r = s.get();
			Assert.assertEquals("p1", r.getId());
			Assert.assertEquals(2003, r.getBirthDate().get(ChronoField.YEAR));
			
		}
	}
	@Test
	public void testReadVisits() throws IOException, ParseException{
		try( RecordSupplier<VisitRow> s = ds.visitTable.open(of,ds.getMeta()) ){
			VisitRow r = s.get();
			Assert.assertEquals("v1", r.getId());
			Assert.assertEquals(2013, r.getStartTime().get(ChronoField.YEAR));
			
		}
	}
	@Test
	public void testReadWideTable() throws IOException, ParseException{
		try( RecordSupplier<WideRow> s = ds.wideTables[0].open(of,ds.getMeta()) ){
			WideRow r = s.get();
			Assert.assertNotNull(r);
			Assert.assertTrue(r.getFacts().size() > 0);
			Observation o = r.getFacts().get(0);
			Assert.assertEquals("natrium", o.getConceptId());
			Assert.assertEquals(Value.Type.Numeric, o.getValue().getType());
			Assert.assertEquals(BigDecimal.valueOf(124), o.getValue().getNumericValue());
			Assert.assertEquals("mmol/L", o.getValue().getUnits());
			ExternalSourceType e = o.getSource();
			Assert.assertNotNull(e);
			Assert.assertEquals("test-1", e.getSourceId());
			
		}
	}
	@Test
	public void testReadEavTable() throws IOException, ParseException{
		try( RecordSupplier<EavRow> s = ds.eavTables[0].open(of,ds.getMeta()) ){
			EavRow r = s.get();
			Assert.assertNotNull(r);
			Assert.assertTrue(r.getFacts().size() > 0);
			Observation o = r.getFacts().get(0);
			// next fact without value. Verify mapped concept
			Assert.assertEquals("f_eav_m_m", o.getConceptId());
			
			// next fact with numeric value
			o = s.get().getFact();
			Assert.assertEquals("f_eav_b", o.getConceptId());
			Assert.assertEquals(Value.Type.Numeric, o.getValue().getType());
			Assert.assertEquals(BigDecimal.valueOf(3.9), o.getValue().getNumericValue());
			ExternalSourceType e = o.getSource();
			Assert.assertNotNull(e);
			Assert.assertEquals("test-1", e.getSourceId());
			
			// skip to last
			for( EavRow n = s.get(); n!=null; n=s.get() ){
				r = n;
			}
			Observation f = r.getFact();
			// should be processed by virtual column map
			Assert.assertEquals("f_eav_x_1", f.getConceptId());
			Assert.assertNull(f.getValue());
			Assert.assertNotNull(f.getEndTime());
		}
	}
}
