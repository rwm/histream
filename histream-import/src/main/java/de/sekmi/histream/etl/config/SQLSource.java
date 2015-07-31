package de.sekmi.histream.etl.config;

import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlElement;

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
	public String[] getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<String[]> rows() {
		// TODO Auto-generated method stub
		return null;
	}

}
