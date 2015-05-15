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
abstract class PostgresExtension<T> implements Extension<T>, Plugin {
	private static final int defaultFetchSize = 10000;
	private static final String driver = "org.postgresql.Driver";
	protected Map<String,String> config;
	protected Connection db;

	protected PostgresExtension(Map<String,String> configuration){
		this.config = configuration;
	}
	
	public static Connection getConnection(Map<String,String> props) throws SQLException, ClassNotFoundException{
		Class.forName(driver);
		Properties jdbcProps = new Properties();
		// TODO put only properties relevant to jdbc
		jdbcProps.putAll(props);
		return DriverManager.getConnection("jdbc:postgresql://"+props.get("host")+":"+props.get("port")+"/"+props.get("database"), jdbcProps);

	}
	protected void open() throws ClassNotFoundException, SQLException{
		db = getConnection(config);
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
