package de.sekmi.histream.export.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import de.sekmi.histream.export.TableWriter;

class Table implements TableWriter{
	private final CSVWriter export;
	private PrintWriter out;
	/**
	 * @param csvWriter CSV writer
	 * @throws IOException IO error
	 */
	Table(CSVWriter csvWriter, String filename) throws IOException {
		export = csvWriter;
		Path file = export.getDirectory().resolve(filename);
		OutputStream os = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
		// no checked exceptions below this point (otherwise must make sure os is closed)
		out = new PrintWriter(new OutputStreamWriter(os, export.getCharset()));
		// TODO does closing the writer also close the output stream?
	}

	private void printRow(String[] data){
		for( int i=0; i<data.length; i++ ){
			if( i != 0 ){
				out.print(export.getFieldSeparator());
			}
			out.print(export.escapeData(data[i]));
		}
		out.println();
	}
	@Override
	public void header(String[] headers) {
		printRow(headers);
	}

	@Override
	public void row(String[] columns) throws IOException {
		printRow(columns);
	}

	@Override
	public void close() throws IOException {
		out.close();
	}
}