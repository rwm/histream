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


import java.io.Closeable;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.i2b2.PostgresPatientStore;
import de.sekmi.histream.impl.ExternalSourceImpl;
public class TestPostgresPatientStore implements Closeable {
	PostgresPatientStore store;
	LocalHSQLDataSource ds;
	
	@Before
	public void createDatabase() throws SQLException, IOException {
		ds = new LocalHSQLDataSource();
		ds.delete();
		ds.createI2b2();
	}

	private void openPatientStore() throws SQLException {
		store = new PostgresPatientStore();
		store.open(ds.getConnection(), "test", new DataDialect());
	}
	@After
	public void cleanupDatabase() throws SQLException, IOException {
		if( store != null ) {
			store.close();
		}
		ds.delete();
	}
	
	@Test
	public void insertPatient() throws IOException, SQLException {
		ExternalSourceType t = new ExternalSourceImpl("junit", Instant.now());
		openPatientStore();
		store.createInstance("ABC001", t);
		store.flush();
		store.close();
		store = null;
		// reload store from database
		openPatientStore();
		// attempt to load previously stored patient
		I2b2Patient p = store.retrieve("ABC001");
		Assert.assertNotNull(p);
	}

	public void open(String  host, int port, String user, String password, String projectId) throws ClassNotFoundException, SQLException{
		store = new PostgresPatientStore();
		store.open(DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/i2b2", user, password),projectId, new DataDialect());
	}

	public PostgresPatientStore getStore(){return store;}

	@Override
	public void close() throws IOException{
		store.close();
	}
	
//	private void open()throws Exception{
//		open("localhost",15432,"i2b2demodata", "demodata","demo");
//	}
	public static void main(String args[]) throws Exception{
		TestPostgresPatientStore test = new TestPostgresPatientStore();
	//	test.open();
		test.open("134.106.36.86",15437, "i2b2crcdata","","AKTIN");
		System.out.println("Current patient cache size: "+test.store.size());
		test.close();
	}
}
