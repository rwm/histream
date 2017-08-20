package de.sekmi.histream;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.junit.Assert;
import org.junit.Test;

public class TestDateTimeAccuracy {

	@Test
	public void testParseYYYYDD(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M.u");
		String text = "02.2003";
		DateTimeAccuracy a = DateTimeAccuracy.parse(formatter, text);
		Assert.assertEquals(ChronoUnit.MONTHS,a.getAccuracy());
		Assert.assertEquals(2, a.get(ChronoField.MONTH_OF_YEAR));
		Assert.assertEquals(2003, a.get(ChronoField.YEAR));
	}
	
	@Test
	public void verifyZoneOffset(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.u[ H[:m[:s]]]");
		DateTimeAccuracy a;
		ZoneId tz = ZoneId.of("Asia/Shanghai"); // China standard time
		a = DateTimeAccuracy.parse(formatter, "01.02.2003");
		Assert.assertEquals("2003-02-01", a.toPartialIso8601(null));
		Assert.assertEquals("2003-02-01", a.toPartialIso8601(tz));
		a = DateTimeAccuracy.parse(formatter, "01.02.2003 13");
		Assert.assertEquals(ChronoUnit.HOURS, a.getAccuracy());
		Assert.assertEquals("2003-02-01T13", a.toPartialIso8601(null));
		Assert.assertEquals("2003-02-01T13+08:00", a.toPartialIso8601(tz));
	}
	
	
	@Test
	public void testFormatExceedsText(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.u[ H[:m[:s]]]");
		formatter.withResolverStyle(ResolverStyle.STRICT);

		DateTimeAccuracy a;
		a = DateTimeAccuracy.parse(formatter, "01.02.2003");
		Assert.assertEquals(ChronoUnit.DAYS,a.getAccuracy());
		
		a = DateTimeAccuracy.parse(formatter, "01.02.2003 13");
		Assert.assertEquals(ChronoUnit.HOURS,a.getAccuracy());

		a = DateTimeAccuracy.parse(formatter, "01.02.2003 13:14");
		Assert.assertEquals(ChronoUnit.MINUTES,a.getAccuracy());
	}

	@Test
	public void verifyIncompleteIsoDateException() throws ParseException{
		try {
			DateTimeAccuracy.parsePartialIso8601("2003-02-01T04:05:06+");
			Assert.fail();
		} catch (ParseException e) {
		}
		// TODO test more aspects of zone offset parsing
		DateTimeAccuracy.parsePartialIso8601("2003-02-01T04:05:06Z");
		DateTimeAccuracy a = DateTimeAccuracy.parsePartialIso8601("2003-02-01T04:05:06+01:00");
		// make sure the date is adjusted to UTC
		Assert.assertEquals(3, a.get(ChronoField.HOUR_OF_DAY));
	}
	@Test
	public void verifyParseExceptionBehavior(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("u");
		// verify expected behavior of DateTimeFormatter
		formatter.parse("2003");
		try{
			formatter.parse("+");
			Assert.fail("Exception expected unexpected input");
		}catch( DateTimeParseException e ){
		}
		try{
			formatter.parse("2003+");
			Assert.fail("Exception expected for unparsed text at end of input");
		}catch( DateTimeParseException e ){
		}
		// verify same behavior for DateTimeAccurecy
		// should not fail below
		DateTimeAccuracy a = DateTimeAccuracy.parse(formatter, "2003");
		Assert.assertEquals(ChronoUnit.YEARS, a.getAccuracy());
		// next two calls should throw exceptions
		try{
			DateTimeAccuracy.parse(formatter, "+");
			Assert.fail("Exception unexpected input");
		}catch( DateTimeParseException e ){
		}
		try{
			DateTimeAccuracy.parse(formatter, "2003+");
			Assert.fail("Exception expected for unparsed text at end of input");
		}catch( DateTimeParseException e ){
		}
	}
	// TODO: further tests
}
