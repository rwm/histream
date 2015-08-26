package de.sekmi.histream.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.stream.StreamSupport;

import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
		debugLog = new FileOutputStream(debugFile);
	}
	@After
	public void cleanLog() throws IOException{
		debugLog.close();
		if( debugFile != null )
			debugFile.delete();
	}
	@Test
	public void testWrite() throws Exception{
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		ObservationSupplier s = t.getExampleSupplier();
		GroupedXMLWriter w = new GroupedXMLWriter(debugLog);
		Meta.transfer(s, w);
		StreamSupport.stream(AbstractObservationParser.nonNullSpliterator(s), false).forEach(w);
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
		StreamSupport.stream(AbstractObservationParser.nonNullSpliterator(s), false).forEach(w);
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
