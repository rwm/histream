package de.sekmi.histream.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.impl.ExternalSourceImpl;
import de.sekmi.histream.impl.Meta;

public class TestXMLWriter {
	private File debugFile;
	private OutputStream debugLog;
	
	@Before
	public void setupLog() throws IOException{
		// for debugging, set debugLog to System.out
		debugFile = File.createTempFile("xmlwriterlog", ".xml");
		// debugLog = System.out
		debugLog = System.out;//new FileOutputStream(debugFile);
	}
	@After
	public void cleanLog() throws IOException{
		if( debugLog != System.out )debugLog.close();
		if( debugFile != null )
			debugFile.delete();
	}
	
	private void printDocument(Document doc) throws TransformerException{
		DOMSource source = new DOMSource(doc);
		Transformer t;
		t = TransformerFactory.newInstance().newTransformer();
		t.transform(source, new StreamResult(debugLog));
	}
	
	private Document createDocument() throws ParserConfigurationException{
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();
		doc.getDomConfig().setParameter("namespaces", true);
		doc.getDomConfig().setParameter("namespace-declarations", true);
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
	@Test
	public void testWriteDOM() throws Exception{

		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		ObservationSupplier s = t.getExampleSupplier();
		Document doc = createDocument();
		GroupedXMLWriter w = new GroupedXMLWriter(new DOMResult(doc));
		Meta.transfer(s, w);
		Streams.transfer(s, w);
		w.close();
		s.close();

		doc.normalizeDocument();
		printDocument(doc);
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
	@Test
	public void testReadWriteIdenticalXML() throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		//dbf.setValidating(true);
		dbf.setNamespaceAware(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setIgnoringComments(true);
		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc1 = db.parse(new File("examples/dwh-jaxb.xml"));
		doc1.normalizeDocument();
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
