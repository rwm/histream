package de.sekmi.histream.impl;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import de.sekmi.histream.i2b2.PostgresVisitStore;

public class TestPostgresVisitStore implements Closeable {
	private PostgresVisitStore store;

	public void open(String  host, int port) throws ClassNotFoundException, SQLException{
		Properties props = new Properties();
		props.put("project", "demo");
		props.put("user", "i2b2demodata");
		props.put("host", host);
		props.put("database", "i2b2");
		props.put("port", Integer.toString(port));
		props.put("password", "");
		store = new PostgresVisitStore(props);
		store.open();		
	}
	
	private void open()throws Exception{
		open("localhost",15432);
	}
	
	@Override
	public void close() throws IOException{
		store.close();
	}
	
	public PostgresVisitStore getStore(){ return store; }
	
	public static void main(String args[]) throws Exception{
		TestPostgresVisitStore t = new TestPostgresVisitStore();
		t.open();
		System.out.println("Current visit cache size: "+t.store.size());
		t.close();
	}
}
