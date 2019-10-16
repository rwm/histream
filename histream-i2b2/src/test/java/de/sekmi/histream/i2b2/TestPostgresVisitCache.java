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
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
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
		assertNotNull(p);
	}
	@Test
	public void accessInsertedWithoutExplicitFlush() throws IOException, SQLException {
		ExternalSourceType t = new ExternalSourceImpl("junit", Instant.now());
		loadCache();
		// create patients
		I2b2Patient pat = store.createPatient("PAT001", t);
		store.createPatient("PAT002", t);
		int num = pat.getNum();
		// create visits
		int vnum = store.createVisit("ENC001", pat, t).getNum();
		store.createVisit("ENC002", pat, t);
		

		// access patients
		pat = store.lookupPatientId("PAT001");
		assertNotNull(pat);
		assertEquals("PAT001",pat.getId());
		// lookup by patient num should return exact same object
		assertTrue(pat == store.lookupPatientNum(num));
		
		// access visits
		I2b2PatientVisit v = store.findVisit("ENC001");
		assertNotNull(v);
		assertEquals("ENC001",v.getId());
		assertNotNull(v.getPatient());
		assertEquals("PAT001", v.getPatientId());
		// lookup by encounter num should return exact same object
		assertTrue(v == store.lookupEncounterNum(vnum));
		
		store.close();
	}

	@Test
	public void insertPatientVisitComboAndReload() throws IOException, SQLException {
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
		assertNotNull(v);
		assertNotNull(v.getPatient());
		assertEquals("PAT001", v.getPatientId());
	}


	@Test
	public void verifyLocalDateTimeInDatabase() throws IOException, SQLException, ParseException {
		ExternalSourceType t = new ExternalSourceImpl("junit", Instant.now());
		ZoneId tz = ZoneId.of("Asia/Shanghai");
		t.setSourceZone(tz);
		loadCache();
		I2b2Patient pat = store.createPatient("PAT001", t);
		pat.setBirthDate(DateTimeAccuracy.parsePartialIso8601("2001-01", tz));
		store.createPatient("PAT002", t);
		store.createVisit("ENC001", pat, t);
		store.createVisit("ENC002", pat, t);
		// changed birthdate should be flushed (stored) during close
		store.close();
		store = null;
		// reload store from database
		loadCache();
		// attempt to load previously stored patient
		I2b2PatientVisit v = store.findVisit("ENC001");
		assertNotNull(v);
		assertNotNull(v.getPatient());
		assertEquals("PAT001", v.getPatientId());
		assertNotNull(v.getPatient().getBirthDate()); // flushed birthdate changed
		DateTimeAccuracy a = v.getPatient().getBirthDate();
		assertEquals(ChronoUnit.MONTHS,a.getAccuracy());
		// verify that the stored birthdate is local time
		Timestamp ts = ds.executeQuerySingleResult("SELECT birth_date FROM patient_dimension WHERE patient_num="+v.getPatientNum(), Timestamp.class);
		assertNotNull(ts);
		assertEquals("2001-01-01T00:00",ts.toLocalDateTime().toString());
	}
}
