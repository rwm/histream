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


import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.ExternalSourceImpl;
public class TestPostgresVisitCache  {
	PostgresPatientVisitCache store;
	LocalHSQLDataSource ds;
	
	@Before
	public void createDatabase() throws SQLException, IOException {
		ds = new LocalHSQLDataSource();
		ds.delete();
		ds.createI2b2();
	}

	private void loadCache() throws SQLException {
		store = new PostgresPatientVisitCache();
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
		loadCache();
		store.createPatient("ABC001", t);
		store.close();
		store = null;
		// reload store from database
		loadCache();
		// attempt to load previously stored patient
		I2b2Patient p = store.lookupPatientId("ABC001");
		Assert.assertNotNull(p);
	}
	@Test
	public void insertPatientVisitCombo() throws IOException, SQLException {
		ExternalSourceType t = new ExternalSourceImpl("junit", Instant.now());
		loadCache();
		I2b2Patient pat = store.createPatient("PAT001", t);
		store.createPatient("PAT002", t);
		store.createVisit("ENC001", pat, t);
		store.createVisit("ENC002", pat, t);
		store.close();
		store = null;
		// reload store from database
		loadCache();
		// attempt to load previously stored patient
		I2b2PatientVisit v = store.findVisit("ENC001");
		Assert.assertNotNull(v);
		Assert.assertNotNull(v.getPatient());
		Assert.assertEquals("PAT001", v.getPatientId());
	}
}
