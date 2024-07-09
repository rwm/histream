package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

import javax.xml.bind.JAXB;

import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.RecordSupplier;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.VisitPatientImpl;

import org.junit.Assert;
import org.junit.Before;

public class TestReadTables {
	private DataSource ds;
	private ObservationFactory of;
	private PatientImpl pat;
	private VisitPatientImpl vis;

	@Before
	public void loadConfiguration() throws IOException, java.text.ParseException{
		URL url = getClass().getResource("/data/test-1-datasource.xml");
		ds = JAXB.unmarshal(url, DataSource.class);
		ds.getMeta().setLocation(url);
		of = new ObservationFactoryImpl();
		Assert.assertEquals(ZoneId.of("Europe/Berlin"), ds.getMeta().getTimezone());
		pat = new PatientImpl("p1");
		vis = new VisitPatientImpl("v1", pat, DateTimeAccuracy.parsePartialIso8601("2001-01-01", ds.getMeta().getTimezone()));
	}
	
	@Test
	public void testReadPatients() throws IOException, ParseException{
		try( RecordSupplier<PatientRow> s = ds.patientTable.open(ds.getMeta()) ){
			PatientRow row = s.get();
			row.createFacts(null, null, of);
			PatientImpl r = row.getPatient();
			Assert.assertEquals("p1", r.getId());
			Assert.assertEquals("1903-02-01", r.getBirthDate().toPartialIso8601(ds.getMeta().getTimezone()));
			Assert.assertEquals(LocalDateTime.of(1903, 2, 1, 0, 0), r.getBirthDate().toLocal(ds.getMeta().getTimezone()));
			Assert.assertEquals("2003-02-11", r.getDeathDate().toPartialIso8601(ds.getMeta().getTimezone()));
			
		}
	}
	@Test
	public void testReadVisits() throws IOException, ParseException{
		try( RecordSupplier<VisitRow> s = ds.visitTable.open(ds.getMeta()) ){
			VisitRow row = s.get();
			row.createFacts(new PatientImpl("p1"), null, of);
			VisitPatientImpl r = row.getVisit();
			Assert.assertEquals("v1", r.getId());
			Assert.assertEquals(2013, r.getStartTime().toInstantMin().atOffset(ZoneOffset.UTC).get(ChronoField.YEAR));
			
		}
	}
	@Test
	public void testReadWideTable() throws IOException, ParseException{
		try( RecordSupplier<WideRow> s = ds.wideTables[0].open(ds.getMeta()) ){
			WideRow r = s.get();
			Assert.assertNotNull(r);
			r.createFacts(vis.getPatient(), vis, of);
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
	public void testReadEavTable() throws IOException, ParseException, java.text.ParseException{
		
		try( RecordSupplier<EavRow> s = ds.eavTables[0].open(ds.getMeta()) ){
			EavRow r = s.get();
			Assert.assertNotNull(r);
			Assert.assertNotNull(r.start);
			r.createFacts(pat, vis, of);

			Assert.assertTrue(r.getFacts().size() > 0);
			Observation o = r.getFacts().get(0);
			// next fact without value. Verify mapped concept
			Assert.assertEquals("f_eav_m_m", o.getConceptId());
			Assert.assertNotNull(o.getStartTime());
			
			// next fact with numeric value
			r = s.get();
			r.createFacts(pat, vis, of);
			o = r.getFact();
			Assert.assertEquals("f_eav_b", o.getConceptId());
			Assert.assertEquals(Value.Type.Numeric, o.getValue().getType());
			Assert.assertEquals(BigDecimal.valueOf(3.9), o.getValue().getNumericValue());
			System.out.println("Start: "+o.getStartTime());
			ExternalSourceType e = o.getSource();
			Assert.assertNotNull(e);
			Assert.assertEquals("test-1", e.getSourceId());
			
			// skip to last
			for( EavRow n = s.get(); n!=null; n=s.get() ){
				r = n;
			}
			r.createFacts(pat, vis, of);
			Observation f = r.getFact();
			// should be processed by virtual column map
			Assert.assertEquals("f_eav_x_1", f.getConceptId());
			Assert.assertNull(f.getValue());
			Assert.assertNotNull(f.getStartTime());
			Assert.assertNotNull(f.getEndTime());
		}
	}
}
