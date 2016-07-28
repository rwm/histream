package de.sekmi.histream.export.csv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import de.sekmi.histream.export.ExportWriter;
import de.sekmi.histream.export.TableWriter;

public class CSVExport implements ExportWriter{

	private char fieldSeparator;
	private Charset charset;
	private Path directory;
	private String filenameExtension;
	
	public CSVExport(Path directory, char fieldSeparator){
		this.charset = Charset.forName("UTF-8");
		this.directory = directory;
		this.fieldSeparator = fieldSeparator;
	}

	public Charset getCharset(){
		return charset;
	}
	public Path getDirectory(){
		return directory;
	}
	public char getFieldSeparator(){
		return fieldSeparator;
	}
	
	private String fileWithExtension(String name){
		return name+filenameExtension;
	}
	/**
	 * Escape data before it is written to the output file.
	 * <p>
	 * If the data contains newline characters or field separators,
	 * these sequences will be escaped.
	 * </p>
	 * @param data data field
	 * @return escaped data field
	 */
	protected String escapeData(String data){
		// TODO do proper escaping
		return data.replace(fieldSeparator, ' ').replace('\n', ' ');
	}
	@Override
	public TableWriter openPatientTable() throws IOException {
		return new Table(this, fileWithExtension("patient"));
	}

	@Override
	public TableWriter openVisitTable() throws IOException {
		return new Table(this, fileWithExtension("encounter"));
	}

	@Override
	public TableWriter openEAVTable(String id) throws IOException {
		return new Table(this, fileWithExtension(id));
	}
}
