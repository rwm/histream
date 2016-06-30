package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.xpath.XPath;

import de.sekmi.histream.export.TableParser;
import de.sekmi.histream.export.TableWriter;

@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractTable {

	@XmlElement(name="column")
	Column[] columns;

	@XmlTransient
	public abstract String getId();
	
	public String[] getHeaders(){
		String[] headers = new String[columns.length];
		for( int i=0; i<columns.length; i++ ){
			headers[i] = columns[i].header;
		}
		return headers;
	}
	public Column getColumn(int index){
		return columns[index];
	}
	public Column[] getColumns(){
		return columns;
	}
	
	public TableParser createParser(TableWriter writer, XPath xpath) throws ExportException{
		return new TableParser(this, writer, xpath);
	}

}