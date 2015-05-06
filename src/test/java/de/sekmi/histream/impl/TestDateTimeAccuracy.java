package de.sekmi.histream.impl;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;

public class TestDateTimeAccuracy {

	static ChronoField[] fields = {ChronoField.YEAR, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH, ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE, ChronoField.MILLI_OF_SECOND};

	private void assertFieldValues(Temporal temporal, int[] values){
		for( int i=0; i<values.length; i++ ){
			Assert.assertEquals(values[i], temporal.get(fields[i]));
		}
	}
	@Test
	public void testParseToString(){
		final String str = "2001-03-04T05:06:07";
		DateTimeAccuracy a = DateTimeAccuracy.parsePartialIso8601(str);
		Assert.assertEquals(ChronoUnit.SECONDS, a.getAccuracy());
		assertFieldValues(a, new int[]{2001,3,4,5,6,7});
		Assert.assertEquals(str, a.toPartialIso8601());
	}
	
	@Test
	public void testPartialDates(){
		final String[] str = {"2001-03-04T05:06","2001-03-04T05","2001-03-04","2001-03","2001"};
		final ChronoUnit[] expectedAccuracy = {ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.DAYS, ChronoUnit.MONTHS, ChronoUnit.YEARS};
		for( int i=0; i<expectedAccuracy.length; i++ ){
			DateTimeAccuracy a = DateTimeAccuracy.parsePartialIso8601(str[i]);
			// verify accuracy
			Assert.assertEquals(expectedAccuracy[i], a.getAccuracy());
			// verify toString
			Assert.assertEquals(str[i], a.toPartialIso8601());
		}
		
	}
}
