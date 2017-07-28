package de.sekmi.histream.i2b2;

import java.text.ParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;

public class TestI2b2Visit {

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
		I2b2Visit v = createVisitWithTimestamps();
		assertEquals(ChronoUnit.SECONDS, v.getEndTime().getAccuracy());
		assertEquals(ChronoUnit.SECONDS, v.getStartTime().getAccuracy());		
	}
	private I2b2Visit createVisitWithTimestamps(){
		I2b2Visit v = new I2b2Visit(0, 0);
		v.setStartTime(createAccurateTimestamp());
		v.setEndTime(createAccurateTimestamp());
		return v;
	}
	@Test
	public void verifyDaysAccuracy(){
		I2b2Visit v = createVisitWithTimestamps();
		// check if the timestamps are both accurate to day
		
		v.setActiveStatusCd(null);
		assertEquals(ChronoUnit.DAYS, v.getEndTime().getAccuracy());
		assertEquals(ChronoUnit.DAYS, v.getStartTime().getAccuracy());

		v = createVisitWithTimestamps();
		v.setActiveStatusCd("");
		assertEquals(ChronoUnit.DAYS, v.getEndTime().getAccuracy());
		assertEquals(ChronoUnit.DAYS, v.getStartTime().getAccuracy());

		
		v = createVisitWithTimestamps();
		v.setActiveStatusCd("YD");
		assertEquals(ChronoUnit.DAYS, v.getEndTime().getAccuracy());
		assertEquals(ChronoUnit.DAYS, v.getStartTime().getAccuracy());
	}
	@Test
	public void verifyMinuteAccuracy(){
		I2b2Visit v = createVisitWithTimestamps();
		v.setActiveStatusCd("I");
		assertEquals(ChronoUnit.MINUTES, v.getStartTime().getAccuracy());
		assertEquals(ChronoUnit.DAYS, v.getEndTime().getAccuracy());

		v = createVisitWithTimestamps();
		v.setActiveStatusCd("T");
		assertEquals(ChronoUnit.DAYS, v.getStartTime().getAccuracy());
		assertEquals(ChronoUnit.MINUTES, v.getEndTime().getAccuracy());
	}
	@Test
	public void verifyHourAndNullAccuracy(){
		I2b2Visit v = createVisitWithTimestamps();
		v.setActiveStatusCd("UH");
		assertEquals(ChronoUnit.HOURS, v.getStartTime().getAccuracy());
		assertEquals(4, v.getStartTime().get(ChronoField.HOUR_OF_DAY));
		assertNull(v.getEndTime());

		v = createVisitWithTimestamps();
		v.setActiveStatusCd("RL");
		assertNull(v.getStartTime());
		assertEquals(ChronoUnit.HOURS, v.getEndTime().getAccuracy());
		assertEquals(4, v.getEndTime().get(ChronoField.HOUR_OF_DAY));
	}
	@Test
	public void verifyMonthAndYearAccuracy(){
		I2b2Visit v = createVisitWithTimestamps();
		v.setActiveStatusCd("XB");
		assertEquals(ChronoUnit.MONTHS, v.getStartTime().getAccuracy());
		assertEquals(ChronoUnit.YEARS, v.getEndTime().getAccuracy());

		v = createVisitWithTimestamps();
		v.setActiveStatusCd("MF");
		assertEquals(ChronoUnit.YEARS, v.getStartTime().getAccuracy());
		assertEquals(ChronoUnit.MONTHS, v.getEndTime().getAccuracy());
	}

}
