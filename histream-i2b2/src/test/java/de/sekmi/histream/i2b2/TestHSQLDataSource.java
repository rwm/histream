package de.sekmi.histream.i2b2;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

public class TestHSQLDataSource {

	@Test
	public void testCreateTables() throws IOException, SQLException {
		LocalHSQLDataSource ds = new LocalHSQLDataSource();
		ds.createI2b2();
		// perform queries
		Assert.assertEquals(0, ds.executeCountQuery("SELECT COUNT(*) FROM observation_fact").intValue());
		// drop database
		ds.delete();
	}
}
