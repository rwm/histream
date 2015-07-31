package de.sekmi.histream.etl.config;

import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)

@XmlSeeAlso({FileSource.class, SQLSource.class})
public abstract class TableSource{
	public abstract String[] getHeaders();
	public abstract Stream<String[]> rows();
}