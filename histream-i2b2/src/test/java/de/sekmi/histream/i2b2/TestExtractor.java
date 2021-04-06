package de.sekmi.histream.i2b2;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.ObservationFactoryImpl;

public class TestExtractor implements DataSource{

	public static void main(String[] args) throws SQLException{
		TestExtractor t = new TestExtractor();
		ObservationFactory of = new ObservationFactoryImpl();
		try( I2b2ExtractorFactory ef = new I2b2ExtractorFactory(t) ){
			
			try( I2b2Extractor e = ef.extract(Timestamp.valueOf("2015-01-16 00:00:00"), Timestamp.valueOf("2015-01-17 00:00:00"), null) ){
				
				e.dump();
			}

			try( I2b2Extractor e = ef.extract(Timestamp.valueOf("2015-01-16 00:00:00"), Timestamp.valueOf("2015-01-17 00:00:00"), Arrays.asList("AKTIN:PLZ")) ){
		
				e.stream().forEach(System.out::println);
			}
			System.out.println("-- now with wildcards");
			ef.setFeature(I2b2ExtractorFactory.ALLOW_WILDCARD_CONCEPT_CODES, Boolean.TRUE);
			try( I2b2Extractor e = ef.extract(Timestamp.valueOf("2015-01-16 00:00:00"), Timestamp.valueOf("2015-01-17 00:00:00"), Arrays.asList("ICD10GM:*","AKTIN:PLZ") ) ){
				
				e.stream().forEach(System.out::println);
			}
			
			
		}
		
		
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return new PrintWriter(System.out);
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Connection getConnection() throws SQLException {
		//final int defaultFetchSize = 10000;
		final String driver = "org.postgresql.Driver";
		final String uri = "jdbc:postgresql://localhost:15432/i2b2";
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
		Connection c = DriverManager.getConnection(uri, "i2b2crcdata", "demodata");
		return c;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Test
	public void extractFromDatabase() throws SQLException, IOException {
		LocalHSQLDataSource ds = new LocalHSQLDataSource();
		try{
			ds.delete();
		}catch( SQLException e ) {
			// ignore non-existing database
		}
		ds.createI2b2();
		ds.loadTestDataset1();
		long count = 0;
		try( I2b2ExtractorFactory f = new I2b2ExtractorFactory(ds) ){
			f.setFeature(I2b2ExtractorFactory.PERSIST_TEMP_TABLES, Boolean.TRUE);
			try( I2b2Extractor e = f.extract(Timestamp.valueOf("2011-01-01 00:00:00"), Timestamp.valueOf("2011-01-03 00:00:00"), Arrays.asList("ICD10GM:Y36.9!")) ){
				count = e.stream().count();
			}
			
		}
		Assert.assertNotEquals(0, count);
		ds.delete();
	}
}
