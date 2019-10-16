package de.sekmi.histream.i2b2;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import de.sekmi.histream.io.GroupedXMLReader;
import de.sekmi.histream.io.TestXMLReader;

public class TestI2b2Inserter {

	DataDialect dialect;
	LocalHSQLDataSource ds;
	PostgresPatientVisitCache store;
	I2b2Inserter insert;

	public void createDatabase() throws SQLException, IOException {
		this.dialect = new DataDialect();
		ds = new LocalHSQLDataSource();
		ds.delete();
		ds.createI2b2();
		store = new PostgresPatientVisitCache();
		store.open(ds.getConnection(), "test", dialect);
		insert = new I2b2Inserter();
		insert.open(ds.getConnection(), dialect);
	}

	public void closeDatabase() throws IOException, SQLException {
		if( store != null ) {
			store.close();
		}
		if( insert != null ) {
			insert.close();
		}
		ds.delete();
		
	}
	@Test
	public void insertPatients() throws SQLException, IOException, XMLStreamException {
		ZoneId tz = ZoneId.of("UTC");
		Objects.requireNonNull(tz);
		createDatabase();
		try( GroupedXMLReader rd = TestXMLReader.getResourceReader("/dwh.xml", null) ){
			
		}
		closeDatabase();
	}
}
