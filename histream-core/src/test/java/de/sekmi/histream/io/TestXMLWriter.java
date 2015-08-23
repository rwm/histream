package de.sekmi.histream.io;

import java.io.File;
import java.io.IOException;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import de.sekmi.histream.ObservationSupplier;

public class TestXMLWriter {

	@Test
	public void testWrite() throws IOException, JAXBException, XMLStreamException, FactoryConfigurationError{
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		ObservationSupplier s = t.getExampleSupplier();
		XMLWriter w = new XMLWriter(System.out);
		StreamSupport.stream(AbstractObservationParser.nonNullSpliterator(s), false).forEach(w);
		w.close();
	}

	@Test
	public void testReadWriteIdenticalXML() throws Exception{

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setIgnoringComments(true);
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc1 = db.parse(new File("examples/dwh-jaxb.xml"));
		doc1.normalizeDocument();

		// TODO compare with generated DOM
		Document doc2 = db.parse(new File("examples/dwh-jaxb.xml"));
		doc2.normalizeDocument();

		Assert.assertTrue(doc1.isEqualNode(doc2));
	}
}
