package de.sekmi.histream.etl.config;


import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.etl.RowSupplier;

@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)

@XmlSeeAlso({FileSource.class, SQLSource.class})
public abstract class TableSource{
	/**
	 * 
	 * @return row supplier
	 * @throws IOException IO errors during row supplier construction
	 */
	public abstract RowSupplier rows() throws IOException;

}