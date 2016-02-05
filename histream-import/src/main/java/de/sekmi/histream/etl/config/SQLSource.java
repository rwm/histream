package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.RowSupplier;

@XmlType(name="sql-query")
public class SQLSource extends TableSource {
	@XmlElement
	String jdbcDriver;
	@XmlElement
	String connectString;
	@XmlElement
	String sqlSelect;
	
	private SQLSource() {
	}
	public SQLSource(String driver, String connectString){
		this();
		this.jdbcDriver = driver;
		this.connectString = connectString;
	}
	@Override
	public RowSupplier rows(Meta meta) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
