package de.sekmi.histream.etl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.regex.Pattern;

public class FileRowSupplier extends RowSupplier {
	private Pattern fieldSeparatorPattern;
	private BufferedReader in;
	private String[] headers;
	private URL url;
	private int lineNo;

	private Instant timestamp;

	public FileRowSupplier(URL location, String fieldSeparator) throws IOException{
		this(location, Pattern.compile(Pattern.quote(fieldSeparator)));
	}

	public FileRowSupplier(URL location, Pattern pattern) throws IOException{
		this.fieldSeparatorPattern = pattern;
		this.url = location;
		this.in = new BufferedReader(new InputStreamReader(location.openStream()));
		// TODO: check whether needed to close underlying InputStream
		
		// load headers
		lineNo = 0; 
		String line = in.readLine();
		lineNo ++;
		
		this.headers = fieldSeparatorPattern.split(line);

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
		String line;
		try {
			line = in.readLine();
			lineNo ++;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		if( line == null ){
			// end of file
			return null;
		}
		
		String[] fields = fieldSeparatorPattern.split(line);
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
