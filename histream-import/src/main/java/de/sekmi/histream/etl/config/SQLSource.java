package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;

import de.sekmi.histream.etl.RowSupplier;

public class SQLSource extends TableSource {
	@XmlElement
	String jdbcDriver;
	@XmlElement
	String connectString;
	@XmlElement
	String sql;
	
	private SQLSource() {
	}
	public SQLSource(String driver, String connectString){
		this();
		this.jdbcDriver = driver;
		this.connectString = connectString;
	}
	@Override
	public RowSupplier rows() {
		// TODO Auto-generated method stub
		return null;
	}
}
