package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import de.sekmi.histream.etl.FileRowSupplier;
import de.sekmi.histream.etl.RowSupplier;

@XmlAccessorType(XmlAccessType.FIELD)
public class FileSource extends TableSource{
	@XmlElement
	URL url;
	
	@XmlElement
	String encoding;
	
	@XmlElement
	String separator;
	
	private FileSource(){
	}
	public FileSource(String url, String separator) throws MalformedURLException{
		this();
		this.url = new URL(url);
		this.separator = separator;
	}
	@Override
	public RowSupplier rows() throws IOException {
		return new FileRowSupplier(url, Pattern.compile(separator));
	}

}
