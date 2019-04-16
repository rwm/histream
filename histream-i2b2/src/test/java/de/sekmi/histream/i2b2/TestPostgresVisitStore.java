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

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.i2b2.PostgresVisitStore;
import de.sekmi.histream.impl.ExternalSourceImpl;

public class TestPostgresVisitStore implements Closeable {
	private PostgresVisitStore vs;
	private PostgresPatientStore ps;
	LocalHSQLDataSource ds;
	
	@Before
	public void createDatabase() throws SQLException, IOException {
		ds = new LocalHSQLDataSource();
		ds.delete();
		ds.createI2b2();
	}

	private void openVisitStore() throws SQLException {
		ps = new PostgresPatientStore();
		ps.open(ds.getConnection(), "test", new DataDialect());
		vs = new PostgresVisitStore();
		vs.open(ds.getConnection(), "test", new DataDialect());
	}
	@After
	public void cleanupDatabase() throws SQLException, IOException {
		if( vs != null ) {
			vs.close();
		}
		if( ps != null ) {
			ps.close();
		}
		ds.delete();
	}

	@Test
	public void insertVisit() throws IOException, SQLException {
		ExternalSourceType t = new ExternalSourceImpl("junit", Instant.now());
		openVisitStore();

		// create patient
		I2b2Patient pat = ps.createInstance("ABC001", t);
		I2b2Visit v = vs.createInstance("VIS001", pat, t);
		v.setStartTime(new DateTimeAccuracy(Instant.now()));
		v = vs.findVisit("VIS001");
		Assert.assertEquals("ABC001",v.getPatientId());
		// TODO reload store from database
		vs.close();
		vs = null;
		ps.close();
		ps = null;

		// attempt to load previously stored patient
		openVisitStore();
		v = vs.findVisit("VIS001");		
		Assert.assertNotNull(v);
		Assert.assertEquals("ABC001",v.getPatientId());
	}



	public void open(String  host, int port) throws ClassNotFoundException, SQLException{
		vs = new PostgresVisitStore();
		vs.open(DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/i2b2", "i2b2demodata", "demodata"), "demo", new DataDialect());
	}
	
	private void open()throws Exception{
		open("localhost",15432);
	}
	
	@Override
	public void close() throws IOException{
		vs.close();
	}
	
	public PostgresVisitStore getStore(){ return vs; }
	
	public static void main(String args[]) throws Exception{
		TestPostgresVisitStore t = new TestPostgresVisitStore();
		t.open();
		System.out.println("Current visit cache size: "+t.vs.size());
		t.close();
	}
}
