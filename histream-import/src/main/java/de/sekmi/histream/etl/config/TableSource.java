package de.sekmi.histream.etl.config;


import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.etl.RowSupplier;

@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)

@XmlSeeAlso({CsvFile.class, SQLSource.class})
public abstract class TableSource{
	/**
	 * Open a row supplier which provides rows. 
	 * This is a resource which must be closed.
	 * 
	 * @param meta meta information
	 * @return row supplier
	 * @throws IOException IO errors during row supplier construction
	 */
	public abstract RowSupplier rows(Meta meta) throws IOException;

}