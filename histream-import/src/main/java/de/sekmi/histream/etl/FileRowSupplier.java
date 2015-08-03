package de.sekmi.histream.etl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.regex.Pattern;

public class FileRowSupplier extends RowSupplier {
	private Pattern fieldSeparatorPattern;
	private BufferedReader in;
	private String[] headers;
	
	public FileRowSupplier(URL location, String fieldSeparator) throws IOException{
		this(location, Pattern.compile(Pattern.quote(fieldSeparator)));
	}
	
	public FileRowSupplier(URL location, Pattern pattern) throws IOException{
		this.fieldSeparatorPattern = pattern;
		this.in = new BufferedReader(new InputStreamReader(location.openStream()));
		// TODO: check whether needed to close underlying InputStream
		
		// load headers
		String line = in.readLine();		
		this.headers = fieldSeparatorPattern.split(line);
	}
	
	@Override
	public String[] getHeaders() throws IOException {
		return headers;
	}

	@Override
	public Object[] get() {
		String line;
		try {
			line = in.readLine();
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

}
