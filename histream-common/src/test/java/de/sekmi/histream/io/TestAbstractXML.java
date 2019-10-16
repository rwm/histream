package de.sekmi.histream.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.ZoneOffset;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.StringValue;
import de.sekmi.histream.impl.VisitPatientImpl;
import de.sekmi.histream.xml.XMLUtils;

/**
 * Root class for XML based test. Provides a function
 * {@link #createDocument()} for DOM based output.
 * <p>
 * Performs JUnit tests to verify basic XML features
 * of the JRE used for compilation. This is because
 * some versions of JDK8 had bugs in their XML processing.
 * @author R.W.Majeed
 *
 */
public class TestAbstractXML {
	protected Path debugFile;
	protected OutputStream debugLog;
	
	@Before
	public void setupLog() throws IOException{
		// for debugging, set debugLog to System.out
		debugFile = Files.createTempFile("xmlwriterlog", ".xml");
		// debugLog = System.out
		debugLog = System.out;//new FileOutputStream(debugFile);
	}
	@After
	public void cleanLog() throws IOException{
		if( debugLog != System.out )debugLog.close();
		if( debugFile != null )
			Files.delete(debugFile);
	}
	

	protected Document createDocument() throws ParserConfigurationException{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		f.setCoalescing(true);
		f.setIgnoringComments(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.getDomConfig().setParameter("namespaces", true);
		doc.getDomConfig().setParameter("namespace-declarations", true);
		// TODO check if DOM can be configured to allow newline characters in values

// not suppoted by default implementation
//		doc.getDomConfig().setParameter("canonical-form", true);
//		doc.getDomConfig().setParameter("element-content-whitespace", false);
		return doc;
	}

	private void testStreamWriterNamespaces(XMLStreamWriter w) throws XMLStreamException{
		w.writeStartDocument();
//		w.writeStartElement("root");
		// write element with default namespace
		w.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, "root", "urn:ns:def");
		w.setDefaultNamespace("urn:ns:def");
		w.writeDefaultNamespace("urn:ns:def");
		// write additional prefix
		w.setPrefix("pre", "urn:ns:pre");
		w.writeNamespace("pre", "urn:ns:pre");


		Assert.assertEquals(XMLConstants.DEFAULT_NS_PREFIX, w.getPrefix("urn:ns:def"));
		// write child with default namespace
		w.writeStartElement("urn:ns:def", "defelem");
		w.writeCharacters("some content");
		w.writeEndElement();

		// write child with other prefix
		w.writeStartElement("urn:ns:pre", "preelem");
		Assert.assertEquals("pre", w.getPrefix("urn:ns:pre"));
		w.writeCharacters("some content");
		w.writeEndElement();
		
		// test default namespace
		Assert.assertEquals(XMLConstants.DEFAULT_NS_PREFIX, w.getPrefix("urn:ns:def"));

		w.writeEndDocument();		
	}

	@Test
	public void testMarshallNewlineInValues() throws ParserConfigurationException, ParseException {
		DOMResult result = new DOMResult(createDocument());
		ObservationFactory factory = new ObservationFactoryImpl();
		PatientImpl patient = new PatientImpl("A");
		VisitPatientImpl visit = new VisitPatientImpl("V", patient, DateTimeAccuracy.parsePartialIso8601("2018", ZoneOffset.UTC.normalized()));
		// manually create observation
		Observation o = factory.createObservation(visit, "B", DateTimeAccuracy.parsePartialIso8601("2018", ZoneOffset.UTC.normalized()));
		o.setValue(new StringValue("1\n2"));
		JAXB.marshal(o, result);
		
	}
	@Test
	public void testStreamWriterNamespaces() throws XMLStreamException, ParserConfigurationException{
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		// enable repairing namespaces to remove duplicate namespace declarations by JAXB marshal
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		
		// test output stream writer
		XMLStreamWriter w = factory.createXMLStreamWriter(System.out);
		testStreamWriterNamespaces(w);
		w.close();
		
		// test DOM stream writer
		DOMResult result = new DOMResult(createDocument());
		w = factory.createXMLStreamWriter(result);
		// will not work for DOM stream writers
		//testStreamWriterNamespaces(w);
		w.close();		
	}
	
	@Test
	public void testNamespaces() throws ParserConfigurationException, XMLStreamException, ObservationException{

		DOMResult result = new DOMResult(createDocument());
		GroupedXMLWriter w = new GroupedXMLWriter(result);
		w.beginStream();
		// doesn't work for DOM writers
		/*
		String pre = w.writer.getPrefix(GroupedXMLWriter.NAMESPACE);
		// make sure prefixes are working..
		//Assert.assertNotNull(pre);
		Assert.assertEquals(XMLConstants.DEFAULT_NS_PREFIX, pre);
		*/
		w.close();
	}

	@XmlRootElement
	private static class ValType{
		@SuppressWarnings("unused")
		public ValType() {} // needed by JAXB
		public ValType(String s) { this.value = s;}
		@XmlValue 
		String value;
	}
	@Test
	public void testWriteDOM2() throws Exception{

		// create document
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
//		f.setNamespaceAware(true);
//		f.setCoalescing(true);
//		f.setIgnoringComments(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.newDocument();
//		doc.getDomConfig().setParameter("namespaces", true);
//		doc.getDomConfig().setParameter("namespace-declarations", true);

		DOMResult dr = new DOMResult(doc);

		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		// enable repairing namespaces to remove duplicate namespace declarations by JAXB marshal
		// this does not work with the DOM stream writer

//		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		XMLStreamWriter writer = factory.createXMLStreamWriter(dr);
		Marshaller marshaller = JAXBContext.newInstance(ValType.class).createMarshaller();
//		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.marshal(new ValType("1\n2"), writer);
		doc.normalizeDocument();
		XMLUtils.printDOM(doc, debugLog);

	}
}
