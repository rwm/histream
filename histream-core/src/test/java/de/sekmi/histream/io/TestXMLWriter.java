package de.sekmi.histream.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
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
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.impl.ExternalSourceImpl;
import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.SimplePatientExtension;
import de.sekmi.histream.impl.SimpleVisitExtension;
import de.sekmi.histream.impl.StringValue;
import de.sekmi.histream.xml.XMLUtils;

public class TestXMLWriter {
	private Path debugFile;
	private OutputStream debugLog;
	
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
	
	private Document createDocument() throws ParserConfigurationException{
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
		factory.registerExtension(new SimplePatientExtension());
		factory.registerExtension(new SimpleVisitExtension());
		
		Observation o = factory.createObservation("A", "B", DateTimeAccuracy.parsePartialIso8601("2018", ZoneOffset.UTC.normalized()));
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
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();

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
	
	/**
	 * GroupedXMLWriter and GroupedXMLReader should write/read metadata, even
	 * if no facts are provided. In this case, the file contains only metadata.
	 *
	 * @throws Exception should not occur
	 */
	@Test
	public void testWriteReadEmptyFile() throws Exception{
		OutputStream out = Files.newOutputStream(debugFile);
		GroupedXMLWriter w = new GroupedXMLWriter(out);
		w.setMeta(ObservationSupplier.META_SOURCE_ID, "123");
		w.close();
		out.close();
		
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();

		// read file
		try( InputStream in = Files.newInputStream(debugFile) ){
			GroupedXMLReader reader = new GroupedXMLReader(t.getFactory(), in);
			Assert.assertEquals("123", reader.getMeta(ObservationSupplier.META_SOURCE_ID));
			Assert.assertNull(reader.get());
			reader.close();
		}
	}
	@XmlRootElement
	private static class ValType{
		public ValType() {}
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
	
	@Test
	public void testWriteDOM() throws Exception{

		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		ObservationSupplier s = t.getExampleSupplier();
		Document doc = createDocument();
		GroupedXMLWriter w = new GroupedXMLWriter(new DOMResult(doc));
		w.setZoneId(ZoneId.of("Asia/Shanghai"));
		Meta.transfer(s, w);
		Streams.transfer(s, w);
		
		// manually create observation
		Observation o = t.getFactory().createObservation("A", "B", DateTimeAccuracy.parsePartialIso8601("2018", ZoneOffset.UTC.normalized()));
		o.setValue(new StringValue("1\n2"));
		ExternalSourceType es = new ExternalSourceImpl("manual", Instant.now());
		o.setSource(es);
		Patient pat = t.getFactory().getExtension(Patient.class).createInstance("A",es);
		o.setExtension(Patient.class, pat);
		o.setExtension(Visit.class, t.getFactory().getExtension(Visit.class).createInstance("V",pat,es));
		w.accept(o);

		w.close();
		s.close();

		doc.normalizeDocument();
		XMLUtils.printDOM(doc, debugLog);
		// read back DOM
		
	}
	@Test
	public void testWriteStream() throws Exception{
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		ObservationSupplier s = t.getExampleSupplier();
		GroupedXMLWriter w = new GroupedXMLWriter(debugLog);
		Meta.transfer(s, w);
		Streams.transfer(s, w);
		w.close();
		s.close();
	}
	@Test
	public void testTimestampsWithZoneOffset() throws Exception{
		ZoneId zone = ZoneId.of("Asia/Shanghai");
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		ObservationSupplier s = t.getExampleSupplier();
		Path temp = Files.createTempFile("eav", ".xml");
		try( OutputStream out = Files.newOutputStream(temp) ){
			GroupedXMLWriter w = new GroupedXMLWriter(out);
			w.setZoneId(zone);
			Meta.transfer(s, w);
			Streams.transfer(s, w);
			w.close();
			s.close();
		}
		System.out.println("XML output written to "+temp);
		// read back XML
		try( InputStream in = Files.newInputStream(temp) ){
			GroupedXMLReader reader = new GroupedXMLReader(t.getFactory(), in);
			Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2014-09-07T18:40:03+0800"), reader.get().getStartTime());
			reader.close();
		}
		// delete temp file
		Files.delete(temp);
		// read without zone
		try( InputStream in = getClass().getResourceAsStream("/dwh.xml") ){
			GroupedXMLReader reader = new GroupedXMLReader(t.getFactory(), in);
			// local timestamps treated as UTC
			Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2014-09-07T18:40:03+0800"), reader.get().getStartTime());
			reader.close();
		}
		try( InputStream in = getClass().getResourceAsStream("/dwh.xml") ){
			// local timestamps treated as CST
			GroupedXMLReader reader = new GroupedXMLReader(t.getFactory(), in, zone);
			Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2014-09-07T10:40:03+0800"), reader.get().getStartTime());
			reader.close();
		}

	}

	@Test
	public void testWriteMeta(){
		Meta meta = new Meta();
		meta.etlStrategy = "lala";
		meta.source = new ExternalSourceImpl("sid", Instant.now());
		meta.order = new Meta.Order(true,false);
		
		JAXB.marshal(meta, debugLog);
		
		meta = new Meta();
		meta.etlStrategy = "lala";
		meta.source = new ExternalSourceImpl("sid", null);
		meta.order = null;
		JAXB.marshal(meta, debugLog);
	}

	private static void removeEmptyText(Document doc) throws XPathExpressionException{
		XPathFactory xpathFactory = XPathFactory.newInstance();
		// XPath to find empty text nodes.
		XPathExpression xpathExp = xpathFactory.newXPath().compile(
		    	"//text()[normalize-space(.) = '']");  
		NodeList emptyTextNodes = (NodeList) 
		        xpathExp.evaluate(doc, XPathConstants.NODESET);

		// Remove each empty text node from document.
		for (int i = 0; i < emptyTextNodes.getLength(); i++) {
		    Node emptyTextNode = emptyTextNodes.item(i);
		    emptyTextNode.getParentNode().removeChild(emptyTextNode);
		}
	}
	// XXX make this work again
	//@Test
	public void testReadWriteIdenticalXML() throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		//dbf.setValidating(true);
		dbf.setNamespaceAware(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setIgnoringComments(true);
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc1 = null;
		try( InputStream in = getClass().getResourceAsStream("/dwh.xml") ){
			doc1 = db.parse(in);
		}
		doc1.normalizeDocument();
		XMLUtils.printDOM(doc1, System.out);
		removeEmptyText(doc1);

		File dest = File.createTempFile("xmlwriter", ".xml");
		FileOutputStream out = new FileOutputStream(dest);
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		ObservationSupplier s = t.getExampleSupplier();
		GroupedXMLWriter w = new GroupedXMLWriter(out);
		Meta.transfer(s, w);
		Streams.transfer(s, w);
		s.close();
		w.close();
		out.close();

		// compare with generated DOM
		Document doc2 = db.parse(dest);
		dest.delete();
		doc2.normalizeDocument();
		removeEmptyText(doc2);

		Assert.assertTrue(doc1.isEqualNode(doc2));
	}
}
