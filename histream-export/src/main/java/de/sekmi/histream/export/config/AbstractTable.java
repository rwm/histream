package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractTable {

	@XmlElement(name="column")
	Column[] columns;
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
}
