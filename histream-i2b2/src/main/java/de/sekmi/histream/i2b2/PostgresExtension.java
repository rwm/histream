package de.sekmi.histream.i2b2;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Extension;
import de.sekmi.histream.Plugin;

/**
 * Extension with database connectivity.
 * Uses configuration properties host, port, database.
 * The configuration is then passed to DriverManager.getConnection, which also uses postgres specific properties user, password, etc.
 * 
 * @author Raphael
 *
 * @param <T> extension instance type
 */
public abstract class PostgresExtension<T> implements Extension<T>, Plugin {
	private static final int defaultFetchSize = 10000;
	private static final String driver = "org.postgresql.Driver";
	protected Map<String,String> config;
	protected Connection db;

	protected PostgresExtension(Map<String,String> configuration){
		this.config = configuration;
	}
	
	public static Connection getConnection(Map<String,String> props, String[]  prefixes) throws SQLException, ClassNotFoundException{
		Properties jdbc = new Properties();
		for( String prefix : prefixes ){
			PostgresExtension.copyProperties(props, prefix, jdbc);			
		}
		return getConnection(jdbc);
	}

	private static Connection getConnection(Properties props) throws SQLException, ClassNotFoundException{
		Class.forName(driver);
		StringBuilder sb = new StringBuilder("jdbc:postgresql://");
		
		if( props.get("host") == null ){
			throw new IllegalArgumentException("host property missing for JDBC connection");
		}else{
			sb.append(props.get("host"));
		}
		if( props.get("port") != null ){
			sb.append(':').append(props.get("port"));
		}
		if( !props.containsKey("database") ){
			throw new IllegalArgumentException("database property missing for JDBC connection");
		}
		sb.append('/').append(props.getProperty("database"));
		
		return DriverManager.getConnection(sb.toString(), props);
	}

	/**
	 * Each key in src that starts with keyPrefix is copied (without the prefix) and its value to dest
	 * @param src map containing key,value pairs
	 * @param keyPrefix prefix to match src keys
	 * @param dest destination properties
	 */
	public static void copyProperties(Map<String,String> src, String keyPrefix, Properties dest){
		src.forEach( 
				(key,value) -> {
					if( key.startsWith(keyPrefix) ){
						dest.put(key.substring(keyPrefix.length()), value);
					}
				} 
		);
	}

	/**
	 * Open a database connection using configuration properties 
	 * with the given prefixes.
	 * @param propertyPrefixes prefix to the configuration properties
	 * @throws ClassNotFoundException if the database driver could not be loaded
	 * @throws SQLException any SQL exceptions
	 */
	protected void openDatabase(String[] propertyPrefixes) throws ClassNotFoundException, SQLException{
		db = getConnection(config, propertyPrefixes);
		prepareStatements();
	}
	
	/**
	 * Open a database connection using a data source
	 * @param ds data source
	 * @throws SQLException SQL exceptions
	 */
	protected void openDatabase(DataSource ds) throws SQLException{
		db = ds.getConnection();
		prepareStatements();
	}

	@Override
	public void close()throws IOException{

		flush();
		
		try {
			// close database
			if( db != null && !db.isClosed() )db.close();
		}catch( SQLException e ){
			throw new IOException(e);
		}
	}

	public static Timestamp inaccurateSqlTimestamp(DateTimeAccuracy dateTime){
		if( dateTime == null )return null;
		else return Timestamp.valueOf(dateTime.getLocal());
	}
	
	/**
	 * Get the configuration setting for fetchSize if configured. Otherwise
	 * the default 10000 is returned.
	 * @return configured fetch size, or 10000 otherwise.
	 */
	public int getFetchSize(){
		if( config.containsKey("fetchSize") ){
			return Integer.parseInt(config.get("fetchSize"));
		}else{
			return defaultFetchSize;
		}
	}

	/**
	 * Write updates to disk. The method is automatically called by {@link #close()}.
	 */
	public abstract void flush();
	
	public abstract void deleteWhereSourceId(String sourceId)throws SQLException;

	protected abstract void prepareStatements() throws SQLException;
}
