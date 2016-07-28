package de.sekmi.histream.export.csv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

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
	
	/**
	 * Create a CSV writer which creates table files
	 * in the specified directory.
	 * @param directory directory where the table files should be created
	 * @param fieldSeparator field separator character.
	 * @param fileSuffix file name suffix (e.g. {@code .csv})
	 */
	public CSVWriter(Path directory, char fieldSeparator, String fileSuffix){
		this.charset = Charset.forName("UTF-8");
		this.directory = directory;
		this.fieldSeparator = fieldSeparator;
		this.filenameExtension = fileSuffix;
		this.patientTableName = "patients";
		this.visitTableName = "visits";
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
		return name+getFileExtension();
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
		return new Table(this, fileWithExtension(patientTableName));
	}

	@Override
	public TableWriter openVisitTable() throws IOException {
		return new Table(this, fileWithExtension(visitTableName));
	}

	@Override
	public TableWriter openEAVTable(String id) throws IOException {
		return new Table(this, fileWithExtension(id));
	}

	@Override
	public void close() {
		// no need to close, we are writing individual files
	}
}
