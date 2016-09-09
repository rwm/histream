package de.sekmi.histream.i2b2.ont;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Consumer;

import de.sekmi.histream.i2b2.sql.VirtualConnection;
import de.sekmi.histream.ontology.Ontology;
import de.sekmi.histream.ontology.OntologyException;

public class SQLGenerator implements Closeable{
	private Import imp;
	private BufferedWriter metaWriter;
	private BufferedWriter dataWriter;

	VirtualConnection metaCon;
	VirtualConnection dataCon;
	
	public SQLGenerator(BufferedWriter metaWriter, BufferedWriter dataWriter, Map<String,String> config) throws IOException, SQLException{
		this.metaWriter = metaWriter;
		this.dataWriter = dataWriter;
		this.metaCon = new VirtualConnection(this::writeMetaSQL);
		this.dataCon = new VirtualConnection(this::writeDataSQL);
		this.imp = new Import(metaCon, dataCon, config);
	}

	public SQLGenerator(Path metaSQL, Path dataSQL, Map<String,String> config, boolean overwrite) throws IOException, SQLException{
		this(Files.newBufferedWriter(metaSQL, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE),//, overwrite?StandardOpenOption.TRUNCATE_EXISTING:StandardOpenOption.CREATE_NEW),
				Files.newBufferedWriter(dataSQL, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE),//, overwrite?StandardOpenOption.TRUNCATE_EXISTING:StandardOpenOption.CREATE_NEW),
				config);
	}
	private void writeMetaSQL(String sql){
		try {
			metaWriter.write(sql);
			metaWriter.write(';');
			metaWriter.newLine();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	private void writeDataSQL(String sql){
		try {
			dataWriter.write(sql);
			dataWriter.write(';');
			dataWriter.newLine();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void setWarningHandler(Consumer<String> handler){
		imp.setWarningHandler(handler);
	}
	public void writeOntologySQL(Ontology ontology) throws SQLException, OntologyException{
		imp.setOntology(ontology);
		imp.processOntology();
	}
	@Override
	public void close() throws IOException {
		metaWriter.close();
		dataWriter.close();
	}
}
