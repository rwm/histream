package de.sekmi.histream.i2b2;

import java.text.ParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ext.Patient.Sex;

public class TestI2b2Patient {

	private DateTimeAccuracy createAccurateTimestamp(){
		try {
			return DateTimeAccuracy.parsePartialIso8601("2001-02-03T04:05:06");
		} catch (ParseException e) {
			throw new AssertionError();
		}
	}
	@Test
	public void verifyAccurateTimestampParsing(){
		assertEquals(ChronoUnit.SECONDS, createAccurateTimestamp().getAccuracy());
	
		// make sure visit has full accuracy by default
		I2b2Patient v = createPatientWithTimestamps();
		assertEquals(ChronoUnit.SECONDS, v.getBirthDate().getAccuracy());
		assertEquals(ChronoUnit.SECONDS, v.getDeathDate().getAccuracy());		
	}
	private I2b2Patient createPatientWithTimestamps(){
		I2b2Patient v = new I2b2Patient(0, Sex.male, createAccurateTimestamp(), createAccurateTimestamp());
		return v;
	}
	@Test
	public void applyNullVitalStatus(){
		I2b2Patient v = createPatientWithTimestamps();
		// check if the timestamps are both accurate to day
		
		v.setVitalStatusCd(null);
		assertEquals(ChronoUnit.DAYS, v.getBirthDate().getAccuracy());
		assertNull(v.getDeathDate());
		assertFalse(v.getDeceased());

		v = createPatientWithTimestamps();
		v.setVitalStatusCd("");
		assertEquals(ChronoUnit.DAYS, v.getBirthDate().getAccuracy());
		assertNull(v.getDeathDate());
		assertFalse(v.getDeceased());
	}
	@Test
	public void verifyMinuteAccuracy(){
		I2b2Patient v = createPatientWithTimestamps();
		v.setVitalStatusCd("I");
		assertEquals(ChronoUnit.MINUTES, v.getBirthDate().getAccuracy());
		assertNull(v.getDeathDate());
		assertFalse(v.getDeceased());

		v = createPatientWithTimestamps();
		v.setVitalStatusCd("T");
		assertEquals(ChronoUnit.DAYS, v.getBirthDate().getAccuracy());
		assertEquals(ChronoUnit.MINUTES, v.getDeathDate().getAccuracy());
	}
	@Test
	public void verifyHourAndNullAccuracy(){
		I2b2Patient v = createPatientWithTimestamps();
		v.setVitalStatusCd("UH");
		assertEquals(ChronoUnit.HOURS, v.getBirthDate().getAccuracy());
		assertEquals(4, v.getBirthDate().get(ChronoField.HOUR_OF_DAY));
		assertNull(v.getDeathDate());

		v = createPatientWithTimestamps();
		v.setVitalStatusCd("RL");
		assertNull(v.getBirthDate());
		assertEquals(ChronoUnit.HOURS, v.getDeathDate().getAccuracy());
		assertEquals(4, v.getDeathDate().get(ChronoField.HOUR_OF_DAY));
	}
	@Test
	public void verifyMonthAndYearAccuracy(){
		I2b2Patient v = createPatientWithTimestamps();
		v.setVitalStatusCd("XB");
		assertEquals(ChronoUnit.MONTHS, v.getBirthDate().getAccuracy());
		assertEquals(ChronoUnit.YEARS, v.getDeathDate().getAccuracy());

		v = createPatientWithTimestamps();
		v.setVitalStatusCd("MF");
		assertEquals(ChronoUnit.YEARS, v.getBirthDate().getAccuracy());
		assertEquals(ChronoUnit.MONTHS, v.getDeathDate().getAccuracy());
	}

}
