package de.sekmi.histream.io;

import java.io.IOException;
import java.time.ZoneId;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import de.sekmi.histream.Observation;
import org.junit.Assert;

public class TestXMLReader {

	public static GroupedXMLReader getResourceReader(String resource, ZoneId localZone) throws IOException {
		try {
			return new GroupedXMLReader(TestXMLReader.class.getResourceAsStream(resource),localZone);
		} catch (XMLStreamException | FactoryConfigurationError | JAXBException e) {
			throw new IOException(e);
		}		
	}
	@Test
	public void verifyLocalDates() throws IOException {
		GroupedXMLReader r = getResourceReader("/dwh.xml", null);
		Observation o = r.get();
		
		Assert.assertEquals("2001-01-01", o.getVisit().getPatient().getBirthDate().toPartialLocalIso(r.getMetaTimezone()));
	}

	@Test
	public void verifyDateTimeTargetPrecision() throws IOException {
		GroupedXMLReader r = getResourceReader("/dwh.xml", null);
		Observation o = r.get();
		
		Assert.assertEquals("2001-01-01", o.getVisit().getPatient().getBirthDate().toPartialLocalIso(r.getMetaTimezone()));
	}

}
