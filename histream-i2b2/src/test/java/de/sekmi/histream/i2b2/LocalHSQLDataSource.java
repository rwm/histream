package de.sekmi.histream.i2b2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.DataSource;

// TODO move to query-i2b2-sql
public class LocalHSQLDataSource implements DataSource{

	private PrintWriter pw;
	private static final  String JDBC_URI = "jdbc:hsqldb:file:target/testdb_crcdata";

	public void delete() throws SQLException {
		try( Connection c = getConnection() ){
			Statement s = c.createStatement();
			s = c.createStatement();
			s.executeQuery("DROP SCHEMA PUBLIC CASCADE");
			s.close();
		}
	}

	private static void executeSQL(Connection dbc, BufferedReader lines) throws SQLException, IOException {
		StringBuilder stmt = new StringBuilder();
		String line;
		while( (line = lines.readLine()) != null ) {
			line = line.trim();
			if( line.trim().startsWith("--") ) {
				// ignore comment lines
				continue;
			}
			if( line.endsWith(";") ) {
				// append without ;
				stmt.append(line.substring(0, line.length()-1));
				// execute
				try( Statement s = dbc.createStatement() ){
					s.executeUpdate(stmt.toString());
				}
				// clear
				stmt = new StringBuilder();
			}else {
				stmt.append(line);					
			}
		}		
	}
	private static void executeSQL(Connection dbc, URL resource) throws SQLException, IOException {
		try( InputStream in = resource.openStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)) )
		{
			executeSQL(dbc, rd);
		}
	}
	/**
	 * Create the database and initialize it with the specified DDL statements
	 * @param sql_ddl SQL DDL statements
	 * @throws SQLException SQL error
	 * @throws IOException IO error reading the DDL
	 */
	public void create(BufferedReader ... sql_ddl) throws SQLException, IOException {
		try( Connection dbc = DriverManager.getConnection(JDBC_URI+";create=true", "SA", "") ){
			for( BufferedReader ddl : sql_ddl ) {
				executeSQL(dbc, ddl);				
			}
		}
	}

	public void createI2b2() throws SQLException, IOException {
		try( InputStream in = TestHSQLDataSource.class.getResourceAsStream("/i2b2_hsqldb_ddl_create.sql");
				BufferedReader rd = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)) )
		{
			this.create(rd);
		}		
	}
	public void loadTestDataset1() throws SQLException, IOException {
		try( Connection dbc = getConnection() ){
			executeSQL(dbc, LocalHSQLDataSource.class.getResource("/i2b2_hsqldb_data.sql"));
		}
	}
	public Integer executeCountQuery(String sql) throws SQLException {
		Integer ret = null;
		try( Connection c = getConnection();
				Statement s = c.createStatement();
				ResultSet rs = s.executeQuery(sql) )
		{
			if( rs.next() ) {
				ret = rs.getInt(1);
			}
		}
		return ret;
	}
	public <T> T executeQuerySingleResult(String sql, Class<T> returnType) throws SQLException {
		try( Connection c = getConnection();
				Statement s = c.createStatement();
				ResultSet rs = s.executeQuery(sql) )
		{
			if( rs.next() ) {
				return rs.getObject(1, returnType);
			}
		}
		return null;
		
	}
	public LocalHSQLDataSource() {
		pw = new PrintWriter(System.out);
	}
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return pw;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.pw = out;
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
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection("SA","");
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return DriverManager.getConnection(JDBC_URI+";ifexists=true", username, password);
	}

}
