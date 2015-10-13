package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.FileRowSupplier;
import de.sekmi.histream.etl.RowSupplier;

/**
 * Table source reading plain text tables.
 * TODO implement escape sequences and quoting OR use opencsv dependency
 * 
 * @author R.W.Majeed
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="plain-file")
public class FileSource extends TableSource{
	/**
	 * Location of the table file. 
	 * A relative location might be specified which 
	 * will be resolved against {@link Meta#getLocation()}.
	 */
	@XmlElement
	String url;
	
	/**
	 * File encoding is not used yet.
	 */
	@XmlElement
	String encoding;
	
	/**
	 * Regular expression pattern for the field separator. e.g. {@code \t} 
	 * The specified string will be processed with {@link Pattern#compile(String)}.
	 */
	@XmlElement
	String separator;
	
	@XmlElement
	String quote;
	
	@XmlElement
	char escape;
	
	private FileSource(){
	}
	public FileSource(String urlSpec, String separator) throws MalformedURLException{
		this();
		this.url = urlSpec;
		this.separator = separator;
	}
	@Override
	public RowSupplier rows(Meta meta) throws IOException {
		URL base = meta.getLocation();
		URL source = (base == null)?new URL(url):new URL(base, url);
		return new FileRowSupplier(source, separator);
	}

}
