package de.sekmi.histream.export.config;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.export.VisitFragmentSupplier;
//import de.sekmi.histream.io.FileObservationProviderTest;
import de.sekmi.histream.xml.NamespaceResolver;
import de.sekmi.histream.xml.XMLUtils;

public class TestVisitFragmentParser {

//	@Test
//	public void verifyVisitFragmentContent() throws Exception{
//		FileObservationProviderTest t = new FileObservationProviderTest();
//		t.initializeObservationFactory();
//		//final Node visitNode = null;
//		try( ObservationSupplier s = t.getExampleSupplier() ){
//			VisitFragmentSupplier sup = new VisitFragmentSupplier(s);
//			Node n = sup.get();
//			
//			System.out.println("nodeName="+n.getNodeName());
//			System.out.println("localName="+n.getLocalName());
//			System.out.println("nsUri="+n.getNamespaceURI());
//			System.out.println("docRootNS="+n.getOwnerDocument().getFirstChild().getNamespaceURI());
//			System.out.println();
//			XMLUtils.printDOM(n, System.out);
//			testXPath(n);
//		}
//	}
	
	private void testXPath(Node visit) throws XPathExpressionException{
		XPathFactory f = XPathFactory.newInstance();
		XPath xp = f.newXPath();
		xp.setNamespaceContext(new NamespaceResolver());
		// selectors always need a prefix in XPath 1
		String ret;
//		ret = (String)xp.evaluate("namespace-uri(./*[6])", visit, XPathConstants.STRING);
//		System.out.println("XPath="+ret);
		ret = (String)xp.evaluate("count(eav:fact)", visit, XPathConstants.STRING);
//		System.out.println("Facts:"+ret);
		Assert.assertEquals("13", ret);
	}
	
}
