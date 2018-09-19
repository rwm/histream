package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.FileRowSupplier;
import de.sekmi.histream.etl.MemoryTable;
import de.sekmi.histream.etl.RowSupplier;

/**
 * Table source reading plain text tables.
 * TODO implement escape sequences and quoting OR use opencsv dependency
 * 
 * @author R.W.Majeed
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="csv-filtered")
public class CsvFiltered extends CsvFile{	
	protected CsvFiltered(){
		super();
	}
	public CsvFiltered(String urlSpec, String separator) throws MalformedURLException{
		super(urlSpec,separator);
	}
	@Override
	public RowSupplier rows(Meta meta) throws IOException {
		FileRowSupplier rows = super.openRowSupplier(meta);
		MemoryTable data = new MemoryTable(rows);
		// TODO sort, filter unique, etc.
		return data;
	}

}
