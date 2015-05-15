package de.sekmi.histream.impl;

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
