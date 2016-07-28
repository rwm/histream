package de.sekmi.histream.export;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.sekmi.histream.export.FactClassAnnotator;
import de.sekmi.histream.impl.ObservationImplJAXBTest;

public class TestFactClassAnnotator {

	@Test
	public void validateAnnotation() throws Exception{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		f.setIgnoringElementContentWhitespace(true);
		DocumentBuilder b = f.newDocumentBuilder();
		Document dom = null;
		try( InputStream in = ObservationImplJAXBTest.class.getResourceAsStream("/fact1.xml") ){
			dom = b.parse(in);
		}
		Element el = (Element)dom.getFirstChild();
		// before annotation
		Assert.assertEquals("fact", el.getLocalName());
		// attribute not available will return empty string
		Assert.assertEquals("", el.getAttribute("class"));
		
		// annotate
		FactClassAnnotator fa = new FactClassAnnotator();
		fa.addMapRule("T:testconcept1", "class1");
		fa.annotateFact(el);

		// after annotation
		Assert.assertEquals("class1", el.getAttribute("class"));

//		XMLUtils.printDOM(dom, System.out);
	}
}
