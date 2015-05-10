package de.sekmi.histream.impl;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import de.sekmi.histream.i2b2.PostgresPatientStore;
public class TestPostgresPatientStore implements Closeable {
	PostgresPatientStore store;
	
	public void open(String  host, int port) throws ClassNotFoundException, SQLException{
		HashMap<String, String> props = new HashMap<>();
		props.put("project", "demo");
		props.put("user", "i2b2demodata");
		props.put("host", host);
		props.put("database", "i2b2");
		props.put("port", Integer.toString(port));
		props.put("password", "");
		store = new PostgresPatientStore(props);
		store.open();
	}

	public PostgresPatientStore getStore(){return store;}

	@Override
	public void close() throws IOException{
		store.close();
	}
	
	private void open()throws Exception{
		open("localhost",15432);
	}
	public static void main(String args[]) throws Exception{
		TestPostgresPatientStore test = new TestPostgresPatientStore();
		test.open();
		System.out.println("Current patient cache size: "+test.store.size());
		test.close();
	}
}
