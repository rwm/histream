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

import de.sekmi.histream.i2b2.PostgresPatientStore;
public class TestPostgresPatientStore implements Closeable {
	PostgresPatientStore store;
	
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
