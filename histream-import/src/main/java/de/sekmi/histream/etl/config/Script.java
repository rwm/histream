package de.sekmi.histream.etl.config;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.time.Instant;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class Script {

	/** 
	 * Character encoding for an external script file 
	 */
	@XmlAttribute
	String charset;
	
	/** 
	 * Media type for the script (optional). {@code text/javascript} is the default value.
	 */
	@XmlAttribute
	String type;
	
	/**
	 * Specifies the URL of an external script file. The URL can be relative
	 * to the configuration.
	 */
	@XmlAttribute
	String src;
	
	@XmlValue
	public String content;
	
	public Reader openReader(Meta meta) throws IOException{
		if( content != null ){
			return new StringReader(content);
		}else if( src != null ){
			URL url = new URL(meta.getLocation(), src);
			return new InputStreamReader(url.openStream(), this.charset);
		}else{
			return null;
		}
	}
	
	public Instant getTimestamp(Meta meta) throws IOException{
		// TODO use last modified of external script files
		return Instant.ofEpochMilli(meta.getLastModified());
	}
}
