package de.sekmi.histream.export.config;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Node;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.export.VisitFragmentSupplier;
import de.sekmi.histream.io.FileObservationProviderTest;

public class TestVisitFragmentParser {

	@Test
	public void verifyVisitFragmentContent() throws Exception{
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		final Node visitNode = null;
		try( ObservationSupplier s = t.getExampleSupplier() ){
			VisitFragmentSupplier sup = new VisitFragmentSupplier(s);
			Node n = sup.get();
			//System.out.println(n.toString());
			printDOM(n);
		}
	}
	
	private void printDOM(Node node) throws UnsupportedEncodingException, TransformerException{
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(node), 
	         new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
	}
}
