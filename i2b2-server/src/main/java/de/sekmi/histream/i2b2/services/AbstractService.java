package de.sekmi.histream.i2b2.services;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class AbstractService {
	private static final Logger log = Logger.getLogger(AbstractService.class.getName());
	public static final String HIVE_NS="http://www.i2b2.org/xsd/hive/msg/1.1/";
	
	/**
	 * Service name (for communication to client)
	 * @return service name, e.g. Workplace Cell
	 */
	public abstract String getName();
	/**
	 * Service version (for communication to client)
	 * @return service version, e.g. 1.700
	 */
	public abstract String getVersion();
	
	DocumentBuilder newDocumentBuilder() throws ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// use schema?
		//factory.setSchema(schema);
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder();
	}
	Document parseRequest(DocumentBuilder builder, InputStream requestBody) throws SAXException, IOException{
		Document dom = builder.parse(requestBody);
		// remove whitespace nodes from message header
		Element root = dom.getDocumentElement();
		try {
			stripWhitespace(root);
		} catch (XPathExpressionException e) {
			log.log(Level.WARNING, "Unable to strip whitespace from request", e);
		}
		return dom;
	}
	private void stripWhitespace(Element node) throws XPathExpressionException{
		XPathFactory xf = XPathFactory.newInstance();
		// XPath to find empty text nodes.
		XPathExpression xe = xf.newXPath().compile("//text()[normalize-space(.) = '']");  
		NodeList nl = (NodeList)xe.evaluate(node, XPathConstants.NODESET);

		// Remove each empty text node from document.
		for (int i = 0; i < nl.getLength(); i++) {
		    Node empty = nl.item(i);
		    empty.getParentNode().removeChild(empty);
		}
	}
	
	private void appendTextNode(Element el, String name, String value){
		Element sub = (Element)el.appendChild(el.getOwnerDocument().createElement(name));
		if( value != null ){
			sub.appendChild(el.getOwnerDocument().createTextNode(value));
		}
	}
	Document createResponse(DocumentBuilder builder, Element request_header){
		Document dom = builder.newDocument();
		Element re = (Element)dom.appendChild(dom.createElementNS(HIVE_NS, "response"));
		appendTextNode(re, "i2b2_version_compatible", "1.1");
		appendTextNode(re, "hl7_version_compatible", "2.4");
		// find sending application from request
		NodeList nl = request_header.getElementsByTagName("sending_application");
		Element el = (Element)re.appendChild(dom.createElement("receiving_application"));
		appendTextNode(el, "application_name", nl.item(0).getFirstChild().getTextContent());
		appendTextNode(el, "application_version", nl.item(0).getLastChild().getTextContent());

		// TODO read response_header.xml template, fill and parse
		
		return dom;
	}
}
