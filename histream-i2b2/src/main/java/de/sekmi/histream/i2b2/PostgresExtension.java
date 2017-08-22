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


import java.sql.SQLException;
import de.sekmi.histream.Extension;
/**
 * Extension with database connectivity.
 * Uses configuration properties host, port, database.
 * The configuration is then passed to DriverManager.getConnection, which also uses postgres specific properties user, password, etc.
 * 
 * @author Raphael
 *
 * @param <T> extension instance type
 */
public abstract class PostgresExtension<T> implements Extension<T> {
//	private static final int defaultFetchSize = 10000;
//	protected Map<String,String> config;
//	protected Connection db;
//
//	protected PostgresExtension(Map<String,String> configuration){
//		this.config = configuration;
//	}
//	
//
//	/**
//	 * Open a database connection using configuration properties 
//	 * with the given prefixes.
//	 * @param propertyPrefixes prefix to the configuration properties
//	 * @throws ClassNotFoundException if the database driver could not be loaded
//	 * @throws SQLException any SQL exceptions
//	 */
//	protected void openDatabase(String[] propertyPrefixes) throws ClassNotFoundException, SQLException{
//		db = getConnection(config, propertyPrefixes);
//		prepareStatements();
//	}
//	
//	/**
//	 * Open a database connection using a data source
//	 * @param ds data source
//	 * @throws SQLException SQL exceptions
//	 */
//	protected void openDatabase(DataSource ds) throws SQLException{
//		db = ds.getConnection();
//		prepareStatements();
//	}
//
//	@Override
//	public void close()throws IOException{
//
//		flush();
//		
//		try {
//			// close database
//			if( db != null && !db.isClosed() )db.close();
//		}catch( SQLException e ){
//			throw new IOException(e);
//		}
//	}
//	/**
//	 * Get the configuration setting for fetchSize if configured. Otherwise
//	 * the default 10000 is returned.
//	 * @return configured fetch size, or 10000 otherwise.
//	 */
//	public int getFetchSize(){
//		if( config.containsKey("fetchSize") ){
//			return Integer.parseInt(config.get("fetchSize"));
//		}else{
//			return defaultFetchSize;
//		}
//	}	

	/**
	 * Write updates to disk.
	 */
	public abstract void flush();
	
	public abstract void deleteWhereSourceId(String sourceId)throws SQLException;

	protected abstract void prepareStatements() throws SQLException;
}
