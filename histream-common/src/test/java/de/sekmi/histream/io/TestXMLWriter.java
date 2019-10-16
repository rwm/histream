package de.sekmi.histream.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.ExternalSourceImpl;
import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.StringValue;
import de.sekmi.histream.impl.VisitPatientImpl;
import de.sekmi.histream.xml.XMLUtils;

public class TestXMLWriter extends TestAbstractXML {

	
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
		w.setMeta(Meta.META_SOURCE_ID, "123", null);
		w.close();
		out.close();
		// read file
		try( InputStream in = Files.newInputStream(debugFile) ){
			GroupedXMLReader reader = new GroupedXMLReader(in, null);
			Assert.assertEquals("123", reader.getMeta(Meta.META_SOURCE_ID, null));
			Assert.assertNull(reader.get());
			reader.close();
		}
	}

	@Test
	public void testWriteDOM() throws Exception{
		ObservationSupplier s = getExampleSupplier();
		Document doc = createDocument();
		GroupedXMLWriter w = new GroupedXMLWriter(new DOMResult(doc));
//		w.setZoneId(ZoneId.of("Asia/Shanghai"));
		Meta.transfer(s, w);
		Streams.transfer(s, w);
		
		ObservationFactoryImpl factory = new ObservationFactoryImpl();
		PatientImpl patient = new PatientImpl("A");
		VisitPatientImpl visit = new VisitPatientImpl("V", patient, DateTimeAccuracy.parsePartialIso8601("2018", ZoneOffset.UTC.normalized()));
		// manually create observation
		Observation o = factory.createObservation(visit, "B", DateTimeAccuracy.parsePartialIso8601("2018", ZoneOffset.UTC.normalized()));
		o.setValue(new StringValue("1\n2"));
		ExternalSourceType es = new ExternalSourceImpl("manual", Instant.now());
		o.setSource(es);
		w.accept(o);

		w.close();
		s.close();

		doc.normalizeDocument();
		XMLUtils.printDOM(doc, debugLog);
		// read back DOM
		
	}
	@Test
	public void testWriteStream() throws Exception{
		ObservationSupplier s = getExampleSupplier();
		GroupedXMLWriter w = new GroupedXMLWriter(debugLog);
		Meta.transfer(s, w);
		Streams.transfer(s, w);
		w.close();
		s.close();
	}

	public GroupedXMLReader getExampleSupplier() throws IOException {
		return TestXMLReader.getResourceReader("/dwh.xml", ZoneId.of("Europe/Berlin"));
	}
	@Test
	public void testTimestampsWithZoneOffset() throws Exception{
//		ZoneId zone = ZoneId.of("Asia/Shanghai");
		ZoneId zone = ZoneId.of("Europe/Berlin");
		ObservationSupplier s = getExampleSupplier();
		Path temp = Files.createTempFile("eav", ".xml");
		try( OutputStream out = Files.newOutputStream(temp) ){
			GroupedXMLWriter w = new GroupedXMLWriter(out);
//			w.setZoneId(zone);
			Meta.transfer(s, w);
			Streams.transfer(s, w);
			w.close();
			s.close();
		}
		System.out.println("XML output written to "+temp);
		// read back XML
		try( InputStream in = Files.newInputStream(temp) ){
			GroupedXMLReader reader = new GroupedXMLReader(in, null);
			Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2014-09-07T10:40:03",zone), reader.get().getStartTime());
			reader.close();
		}
		// delete temp file
//		Files.delete(temp);
		// read without zone
		try( InputStream in = getClass().getResourceAsStream("/dwh-no-zone.xml") ){
			GroupedXMLReader reader = new GroupedXMLReader(in, null);
			// local timestamps treated as UTC
			Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2014-09-07T10:40:03Z",null), reader.get().getStartTime());
			reader.close();
		}
		try( InputStream in = getClass().getResourceAsStream("/dwh-no-zone.xml") ){
			// local timestamps treated as CST
			zone = ZoneId.of("Asia/Shanghai");
			GroupedXMLReader reader = new GroupedXMLReader(in, zone);
			Assert.assertEquals(DateTimeAccuracy.parsePartialIso8601("2014-09-07T10:40:03+0800",null), reader.get().getStartTime());
			reader.close();
		}

	}

	@Test
	public void testWriteMeta(){
		Meta meta = new Meta();
		meta.set(Meta.META_ETL_STRATEGY, "lala", null);
		meta.setSource(new ExternalSourceImpl("sid", Instant.now()));
//		meta.order = new Meta.Order(true,false);
		
		JAXB.marshal(meta, debugLog);
		
		meta = new Meta();
		meta.set(Meta.META_ETL_STRATEGY, "lala", null);
		meta.setSource(new ExternalSourceImpl("sid", null));
//		meta.order = null;
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
		ObservationSupplier s = getExampleSupplier();
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
