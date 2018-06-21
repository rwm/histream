package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
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
@XmlType(name="csv-file")
public class CsvFile extends TableSource{
	/**
	 * Location of the table file. 
	 * A relative location might be specified which 
	 * will be resolved against {@link Meta#getLocation()}.
	 */
	@XmlElement
	String url;
	
	/**
	 * Encoding to use for reading text files
	 */
	@XmlElement
	String encoding;
	
	/**
	 * Regular expression pattern for the field separator. e.g. {@code \t} 
	 * The specified string will be processed with {@link Pattern#compile(String)}.
	 */
	@XmlElement
	String separator;
	
//	@XmlElement
//	String quote;
//	
//	@XmlElement
//	char escape;
	
	private CsvFile(){
	}
	public CsvFile(String urlSpec, String separator) throws MalformedURLException{
		this();
		this.url = urlSpec;
		this.separator = separator;
	}
	@Override
	public RowSupplier rows(Meta meta) throws IOException {
		// resolve url relative to base url from metadata
		URL base = meta.getLocation();
		URL source = (base == null)?new URL(url):new URL(base, url);
		// determine charset
		Charset charset;
		if( encoding != null ) {
			charset = Charset.forName(encoding);
		}else{
			// if not defined, use system charset
			charset = Charset.defaultCharset();
		}
		return new FileRowSupplier(source, separator, charset);
	}

}
