package de.sekmi.histream.impl;

import java.io.StringWriter;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;


import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import de.sekmi.histream.Observation;
import de.sekmi.histream.eval.Engine;
import de.sekmi.histream.eval.ScriptException;

/**
 * Evaluate XPath expressions for observations.
 * <p>
 * Each observations is converted to XML via JAXB with {@link ObservationImpl}.
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
	private XPath xpath;
	private JAXBContext jaxb;
	private Marshaller marshaller;
	
	public XPathEvaluator() throws JAXBException{
		this.factory = XPathFactory.newInstance();
		this.xpath = factory.newXPath();
		this.jaxb = JAXBContext.newInstance(ObservationImpl.class);
		this.marshaller = jaxb.createMarshaller();
		//this.marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
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
			marshaller.marshal(fact, dom);
			//marshaller.marshal(fact, System.out);
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
