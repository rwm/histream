package de.sekmi.histream.export;

import java.io.IOException;
import java.util.Objects;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import de.sekmi.histream.export.config.AbstractTable;
import de.sekmi.histream.export.config.Column;
import de.sekmi.histream.export.config.ExportException;

/**
 * Parses a XML fragment for row values
 * @author R.W.Majeed
 *
 */
public class TableParser implements AutoCloseable{
	private AbstractTable table;
	private XPath xpath;
	private XPathExpression[] xpaths;
	private TableWriter writer;

	public TableParser(AbstractTable table, TableWriter writer, XPath xpath) throws ExportException{
		this.table = table;
		this.writer = writer;
		this.xpath = xpath;
		Objects.requireNonNull(table);
		Objects.requireNonNull(writer);
		compileXPaths();
		writeHeaders();
	}

	private void compileXPaths() throws ExportException{
		Column[] columns = table.getColumns();
		xpaths = new XPathExpression[columns.length];
		for( int i=0; i<columns.length; i++ ){
			try {
				xpaths[i] = xpath.compile(columns[i].getXPath());
			} catch (XPathExpressionException e) {
				throw new ExportException("Unable to compile XPath expression for "+table.getId()+"."+columns[i].getHeader(), e);
			}
		}
	}
	
	private void writeHeaders(){
		writer.header(table.getHeaders());
	}
	
	public void writeRow(Node node) throws ExportException{
		try {
			writer.row(valuesForFragment(node));
		} catch (IOException e) {
			throw new ExportException("Unable to write row", e);
		}
	}
	private String[] valuesForFragment(Node node) throws ExportException{
		Column[] columns = table.getColumns();
		String[] values = new String[columns.length];
		for( int i=0; i<columns.length; i++ ){
			try {
				values[i] = (String)xpaths[i].evaluate(node, XPathConstants.STRING);
			} catch (XPathExpressionException e) {
				throw new ExportException("XPath evaluation failed for "+table.getId()+"."+columns[i].getHeader(), e);
			}
		}
		return values;
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
}