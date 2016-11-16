package de.sekmi.histream.i2b2.ont;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class PostgresUtil {
	private static final String DRIVER_CLASS = "org.postgresql.Driver";
	public static Connection getConnection(Map<String,String> props, String[]  prefixes) throws SQLException, ClassNotFoundException{
		Properties jdbc = new Properties();
		for( String prefix : prefixes ){
			copyProperties(props, prefix, jdbc);			
		}
		return getConnection(jdbc);
	}

	private static Connection getConnection(Properties props) throws SQLException, ClassNotFoundException{
		Class.forName(DRIVER_CLASS);
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

}
