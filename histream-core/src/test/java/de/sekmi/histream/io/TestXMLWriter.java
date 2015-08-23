package de.sekmi.histream.io;

import java.io.IOException;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import de.sekmi.histream.ObservationSupplier;

public class TestXMLWriter {

	@Test
	public void testWrite() throws IOException, JAXBException, XMLStreamException, FactoryConfigurationError{
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		ObservationSupplier s = t.getExampleSupplier();
		XMLWriter w = new XMLWriter(null);
		StreamSupport.stream(AbstractObservationParser.nonNullSpliterator(s), false).forEach(w);
		w.close();
	}
}
