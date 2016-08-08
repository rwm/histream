package de.sekmi.histream.export;

import java.io.IOException;
import java.util.Objects;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.sekmi.histream.export.config.EavTable;
import de.sekmi.histream.export.config.ExportException;

public class EavTableParser extends TableParser {
	private XPathExpression factSelector;
	
	public EavTableParser(EavTable table, TableWriter writer, XPath xpath) throws ExportException, IOException {
		super(table, writer, xpath);
		Objects.requireNonNull(table.getXPath(), "xpath expression required for eav-table[id='"+table.getId()+"']/@xpath");
		try {
			factSelector = xpath.compile(table.getXPath());
		} catch (XPathExpressionException e) {
			throw new ExportException("Unable to compile xpath attribute '"+table.getXPath()+"' for table "+table.getId());
		}
	}
	
	@Override
	public void processNode(Node visit) throws ExportException, IOException {
		// select facts with xpath expression on visit element
		NodeList nl;
		try {
			nl = (NodeList)factSelector.evaluate(visit, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new ExportException("XPath evaluation failed for eav-table[@id='"+getTable().getId()+"']/@xpath", e);
		}
		for( int i=0; i< nl.getLength(); i++ ){
			Node n = nl.item(i);
			// make sure only fact elements are selected
			if( n.getNodeType() != Node.ELEMENT_NODE || !n.getLocalName().equals("fact") ){
				throw new ExportException("xpath for eav table '"+getTable().getId()+" must select only 'fact' elements. instead found "+n.toString());
			}
			// write table row for each selected fact
			super.processNode(n);
		}
	}


}
