package de.sekmi.histream.impl;

import java.text.ParseException;

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
	public void testParseToString() throws ParseException{
		final String[] str = new String[]{"2001-03-04T10:16:07", "2001-03-04T10:16:00"};
		DateTimeAccuracy a = DateTimeAccuracy.parsePartialIso8601(str[0]);
		Assert.assertEquals(ChronoUnit.SECONDS, a.getAccuracy());
		assertFieldValues(a, new int[]{2001,3,4,10,16,7});
		for( String s : str ){
			a = DateTimeAccuracy.parsePartialIso8601(s);
			Assert.assertEquals(s, a.toPartialIso8601());
		}
	}
	
	@Test
	public void testPartialDates() throws ParseException{
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
