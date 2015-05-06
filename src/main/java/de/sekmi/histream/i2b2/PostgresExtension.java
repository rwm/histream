package de.sekmi.histream.i2b2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
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
 * @param <T>
 */
public abstract class PostgresExtension<T> implements Extension<T>, Plugin {
	static final String driver = "org.postgresql.Driver";
	protected Properties config;
	protected Connection db;

	public PostgresExtension(Properties configuration){
		this.config = configuration;
	}
	public void open() throws ClassNotFoundException, SQLException{
		Class.forName(driver);
		db = DriverManager.getConnection("jdbc:postgresql://"+config.getProperty("host")+":"+config.getProperty("port")+"/"+config.getProperty("database"), config);
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
	 * Write updates to disk. The method is automatically called by {@link #close()}.
	 */
	public abstract void flush();
	
	public abstract void deleteWhereSourceId(String sourceId)throws SQLException;

	protected abstract void prepareStatements() throws SQLException;
}
