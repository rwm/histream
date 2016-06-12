package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractTable {

	public abstract String[] getHeaders();
	public abstract AbstractColumn getColumn(int index);
	public abstract AbstractColumn getColumnByHeader(String header);
}
