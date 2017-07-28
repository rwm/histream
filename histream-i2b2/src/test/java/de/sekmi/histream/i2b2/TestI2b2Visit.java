package de.sekmi.histream.i2b2;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;

public class TestI2b2Visit {

	private DateTimeAccuracy createAccurateTimestamp(){
		try {
			return DateTimeAccuracy.parsePartialIso8601("20010203T04:05:06");
		} catch (ParseException e) {
			throw new AssertionError();
		}
	}
	@Test
	public void verifyAccurateTimestampParsing(){
		assertEquals(ChronoUnit.SECONDS, createAccurateTimestamp().getAccuracy());
	}
	public void verifyVisitTimestampSerialisation() throws ParseException{
		I2b2Visit v = new I2b2Visit(0, 0);
		v.setStartTime(createAccurateTimestamp());
		v.setEndTime(createAccurateTimestamp());
		v.setActiveStatusCd(null);
		// TODO check if the timestamps are both accurate to day
		// TODO more tests
	}
}
