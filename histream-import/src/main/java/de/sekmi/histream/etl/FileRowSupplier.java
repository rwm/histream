package de.sekmi.histream.etl;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.Instant;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

public class FileRowSupplier extends RowSupplier {
	private CSVReader in;
	private String[] headers;
	private URL url;
	private int lineNo;

	private Instant timestamp;

	public FileRowSupplier(URL location, String fieldSeparator, Charset charset) throws IOException{
		if( fieldSeparator.length() > 1 ){
			if( fieldSeparator.equals("\\t") ){
				fieldSeparator = "\t";
			}else{
				throw new IllegalArgumentException("Only single character or '\\t' allowed for separator");
			}
		}
		this.url = location;
		this.in = new CSVReader(new InputStreamReader(location.openStream(), charset),fieldSeparator.charAt(0), CSVParser.DEFAULT_QUOTE_CHARACTER, (char)0);
		
		// TODO: check whether needed to close underlying InputStream
		
		// load headers
		lineNo = 0; 
		this.headers = in.readNext();
		lineNo ++;

		determineFileTimestamp(location);
	}

	private void determineFileTimestamp(URL url) throws IOException{
		if( url.getProtocol().equals("file") ){
			// get file timestamp
			
			/*
			Path path;
			try {
				path = new File(url.toURI()).toPath();
				//Paths.get(url.getPath()); does not work with URLs like file:/C:/lala
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
			BasicFileAttributes atts = Files.readAttributes(path, BasicFileAttributes.class);
			this.timestamp = atts.creationTime().toInstant();
			*/
			this.timestamp = Instant.ofEpochMilli(new File(url.getPath()).lastModified());
		}else if( url.getProtocol().equals("jar") ){
			// XXX this will return the jar's timestamp
			// TODO find timestamp of actual file within the jar
			URLConnection c = url.openConnection();
			this.timestamp = Instant.ofEpochMilli(c.getLastModified());
		}else{
			throw new IOException("Unable to determine timestamp for URL: "+url);
			// TODO e.g. use URLConnection to get timestamp
		}

	}
	@Override
	public String[] getHeaders() {
		return headers;
	}

	@Override
	public Object[] get() {
		
		String[] fields;
		try {
			fields = in.readNext();
			lineNo ++;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return fields;
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public Instant getTimestamp() {
		return timestamp;
	}

	@Override
	public String getLocation() {
		return url.toString()+":"+lineNo;
	}

}
