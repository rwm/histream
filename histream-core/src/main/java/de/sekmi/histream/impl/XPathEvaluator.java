package de.sekmi.histream.impl;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;


import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.sekmi.histream.Observation;
import de.sekmi.histream.eval.Engine;
import de.sekmi.histream.eval.ScriptException;

/**
 * Evaluate XPath expressions for observations.
 * <p>
 * Each observations is converted to XML via JAXB with {@link ObservationImpl}.
 * In XPath 1.0, null namespace means no namespace. Therefore all namespace information is removed from the DOM.
 *  
 * The XML looks like:
 * <pre>{@code
 * <fact patient="XX12345" encounter="XXE12345" concept="T:group:1" start="2014-01-01T010:30:00" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <value xsi:type="string">groupvalue</value>
    <modifier code="T:mod:3">
        <value xsi:type="numeric" unit="mm">78.9</value>
    </modifier>
    <modifier code="T:mod:2">
        <value xsi:type="string">def456</value>
    </modifier>
    <modifier code="T:mod:1"/>
</fact>
    }</pre>
 * @author Raphael
 *
 */
public class XPathEvaluator implements Engine{
	private XPathFactory factory;
	XPath xpath;
	private JAXBContext jaxb;
	private Marshaller marshaller;
	private static final NamespaceContext namespaceContext = new NamespaceContext() {
		@Override
		public Iterator<?> getPrefixes(String namespaceURI) {
			String prefix = getPrefix(namespaceURI);
			if( prefix == null )return Arrays.asList().iterator();
			else if( namespaceURI.equals(ObservationImpl.XML_NAMESPACE) ){
				return Arrays.asList(prefix,"f").iterator();
			}else return Arrays.asList(prefix).iterator();
		}
		
		@Override
		public String getPrefix(String namespaceURI) {
			switch( namespaceURI ){
			case ObservationImpl.XML_NAMESPACE:
				return XMLConstants.DEFAULT_NS_PREFIX;
			case XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI:
				return "xsi";
			case XMLConstants.XML_NS_URI:
				return XMLConstants.XML_NS_PREFIX;
			case XMLConstants.XMLNS_ATTRIBUTE_NS_URI:
				return XMLConstants.XMLNS_ATTRIBUTE;
			}
			return null;
		}
		
		@Override
		public String getNamespaceURI(String prefix) {
			switch( prefix ){
			case XMLConstants.DEFAULT_NS_PREFIX:
			case "f":
				return ObservationImpl.XML_NAMESPACE;
			case "xsi":
				return XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
			case XMLConstants.XML_NS_PREFIX:
				return XMLConstants.XML_NS_URI;
			case XMLConstants.XMLNS_ATTRIBUTE:
				return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
			}
			return XMLConstants.NULL_NS_URI;
		}
	};
	
	public XPathEvaluator() throws JAXBException{
		this.factory = XPathFactory.newInstance();
		this.xpath = factory.newXPath();
		this.xpath.setNamespaceContext(namespaceContext);
		this.jaxb = JAXBContext.newInstance(ObservationImpl.class);
		this.marshaller = jaxb.createMarshaller();
		//this.marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
	}
	
	public static final String toXMLString(Observation fact){
		StringWriter w = new StringWriter();
		JAXB.marshal(fact, w);
		return w.toString();
	}
	public String evaluateToString(String expression, Observation fact) throws ScriptException{
		try {
			return (String)xpath.evaluate(expression, getObservationNode(fact), XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new ScriptException(e);
		}
	}
	private Node getObservationNode(Observation fact) throws ScriptException{
		DOMResult dom = new DOMResult();
		try {
			/*DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			f.setNamespaceAware(false);
			Document d = f.newDocumentBuilder().newDocument();
			DOMConfiguration dc = d.getDomConfig();
			dc.setParameter("namespace-declarations", false);
			dc.setParameter("namespaces", false);
			dom = new DOMResult(d);
			marshaller.marshal(fact, d.getDocumentElement());
			System.out.println("XX:"+dom.getNode().getOwnerDocument().getDomConfig().getParameter("namespaces"));
			TODO: find a way to produce a DOM without namespaces, then remove namespaces from XPaths in XPathEvaluatorTest and test-mapping.ttl
			*/
			marshaller.marshal(fact, dom);
			marshaller.marshal(fact, System.out);
		} catch (JAXBException e) {
			throw new ScriptException(e);
		}
		return dom.getNode();
	}
	@Override
	public boolean test(String expression, Observation fact) throws ScriptException{
		Boolean ret;
		try {
			ret = (Boolean)xpath.evaluate(expression, getObservationNode(fact), XPathConstants.BOOLEAN);
		} catch (XPathExpressionException e) {
			throw new ScriptException(e);
		}
		return ret.booleanValue();
	}

	@Override
	public void validateExpressionSyntax(String expression)
			throws ScriptException {
		// TODO test expression with dummy observation

	}
}
