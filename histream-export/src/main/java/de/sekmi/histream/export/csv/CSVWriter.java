package de.sekmi.histream.export.csv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import de.sekmi.histream.export.ExportWriter;
import de.sekmi.histream.export.TableWriter;

/**
 * Write CSV files to a specified directory.
 * <p>
 * By default, the patients table is named {@code "patients" + getFileExtension()}
 * and the visits table is named {@code "visits" + getFileExtension()}. The EAV
 * tables are named by their specified ID (also with file extension). The patient and
 * visit table names can be changed via {@link #setPatientTableName(String)} and
 * {@link #setVisitTableName(String)}.
 * </p>
 * <p>
 * The {@link #close()} method does nothing, since individual files are written
 * for each table, which in turn must be closed individually.
 * </p>
 * <p>
 * TODO maybe allow appending to files
 * </p>
 * @author R.W.Majeed
 *
 */
public class CSVWriter implements ExportWriter{

	private char fieldSeparator;
	private Charset charset;
	private Path directory;
	private String filenameExtension;
	private String patientTableName;
	private String visitTableName;
	private String nullString;
	
	/**
	 * Create a CSV writer which creates table files
	 * in the specified directory.
	 * @param directory directory where the table files should be created. Must be non-null.
	 * @param fieldSeparator field separator character. Single space and newline are not allowed as separator characters.
	 * @param fileSuffix file name suffix (e.g. {@code .csv})
	 */
	public CSVWriter(Path directory, char fieldSeparator, String fileSuffix){
		Objects.requireNonNull(directory, "Directory path required");
		this.charset = Charset.forName("UTF-8");
		this.directory = directory;
		this.fieldSeparator = fieldSeparator;
		this.filenameExtension = fileSuffix;
		this.nullString = ""; // write null values as empty strings
		this.patientTableName = "patients";
		this.visitTableName = "visits";
		if( fieldSeparator == ' ' || fieldSeparator == '\n' ){
			throw new IllegalArgumentException("Single space and line separator not allowed as field separator");
		}
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
	
	public String fileNameForTable(String tableName){
		return tableName+getFileExtension();
	}

	/**
	 * Set the file name extension for table files (e.g. {@code .csv}).
	 * The extension is appended to table names to produce file names.
	 * @param suffix file name suffix (usually beginning with a dot)
	 */
	public void setFileExtension(String suffix){
		this.filenameExtension = suffix;
	}
	public String getFileExtension(){
		return filenameExtension;
	}
	public void setVisitTableName(String tableName){
		this.visitTableName = tableName;
	}
	public void setPatientTableName(String tableName){
		this.patientTableName = tableName;
	}
	public String getPatientTableName(){
		return this.patientTableName;
	}
	public String getVisitTableName(){
		return this.visitTableName;
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
		if( data == null ){
			return nullString;
		}
		// TODO do proper escaping
		return data.replace(fieldSeparator, ' ').replace('\n', ' ').replace('\r', ' ');
	}
	@Override
	public TableWriter openPatientTable() throws IOException {
		return new Table(this, fileNameForTable(getPatientTableName()));
	}

	@Override
	public TableWriter openVisitTable() throws IOException {
		return new Table(this, fileNameForTable(getVisitTableName()));
	}

	@Override
	public TableWriter openEAVTable(String id) throws IOException {
		return new Table(this, fileNameForTable(id));
	}

	@Override
	public void close() {
		// no need to close, we are writing individual files
	}
}
