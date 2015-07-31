package de.sekmi.histream.etl.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class FileSource extends TableSource{
	@XmlElement
	URL url;
	
	@XmlElement
	String type;
	
	private FileSource(){
	}
	public FileSource(String url, String type) throws MalformedURLException{
		this();
		this.url = new URL(url);
		this.type = type;
	}
	@Override
	public String[] getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<String[]> rows() {
		return null;
	}

}
