package de.sekmi.histream;

import java.text.ParsePosition;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

import org.junit.Assert;
import org.junit.Test;

public class TestDateTimeAccuracy {

	@Test
	public void testParseYYYYDD(){
		//DateTimeAccuracy a = DateTimeAccuracy.parse(formatter, text)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M.u");
		String text = "02.2003";
		DateTimeAccuracy a = DateTimeAccuracy.parse(formatter, text);
		Assert.assertEquals(ChronoUnit.MONTHS,a.getAccuracy());
		Assert.assertEquals(2, a.get(ChronoField.MONTH_OF_YEAR));
		Assert.assertEquals(2003, a.get(ChronoField.YEAR));
	}
	// TODO: further tests
}
