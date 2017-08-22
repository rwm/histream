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

import de.sekmi.histream.i2b2.PostgresVisitStore;

public class TestPostgresVisitStore implements Closeable {
	private PostgresVisitStore store;

	public void open(String  host, int port) throws ClassNotFoundException, SQLException{
		store = new PostgresVisitStore();
		store.open(DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/i2b2", "i2b2demodata", "demodata"), "demo", new DataDialect());
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
