package de.sekmi.histream.export.config;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.xpath.XPath;

import de.sekmi.histream.export.EavTableParser;
import de.sekmi.histream.export.TableWriter;

public class EavTable extends AbstractTable{

	@XmlID
	@XmlAttribute
	String id;
	
	@XmlAttribute
	String xpath;

	@Override
	public String getId() {
		return id;
	}
	
	public String getXPath(){
		return xpath;
	}

	@Override
	public EavTableParser createParser(TableWriter writer, XPath xpath) throws ExportException, IOException {
		return new EavTableParser(this, writer, xpath);
	}
}
