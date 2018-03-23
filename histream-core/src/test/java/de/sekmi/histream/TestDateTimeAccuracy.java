package de.sekmi.histream;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;

import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

import de.sekmi.histream.xml.DateTimeAccuracyAdapter;

public class TestDateTimeAccuracy {

	@Test
	public void testParseYYYYDD(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M.u");
		String text = "02.2003";
		DateTimeAccuracy a = DateTimeAccuracy.parse(formatter, text, ZoneOffset.UTC);
		Assert.assertEquals(ChronoUnit.MONTHS,a.getAccuracy());
		TemporalAccessor ac = a.toInstantMin().atOffset(ZoneOffset.UTC);
		Assert.assertEquals(2, ac.get(ChronoField.MONTH_OF_YEAR));
		Assert.assertEquals(2003, ac.get(ChronoField.YEAR));
	}
	
	@Test
	public void verifyZoneOffset(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.u[ H[:m[:s]]]");
		DateTimeAccuracy a;
		ZoneId tz = ZoneId.of("Asia/Shanghai"); // China standard time
		a = DateTimeAccuracy.parse(formatter, "01.02.2003", ZoneOffset.UTC);
		Assert.assertEquals("2003-02-01", a.toPartialIso8601(null));
		Assert.assertEquals("2003-02-01", a.toPartialIso8601(tz));
		a = DateTimeAccuracy.parse(formatter, "01.02.2003 13", ZoneOffset.UTC);
		Assert.assertEquals(ChronoUnit.HOURS, a.getAccuracy());
		Assert.assertEquals("2003-02-01T13", a.toPartialIso8601(null));
		Assert.assertEquals("2003-02-01T21+0800", a.toPartialIso8601(tz));
	}
	
	@Test
	public void verifyDateTimeFormatter(){
		TemporalAccessor a;
		DateTimeFormatter f = DateTimeFormatter.ISO_DATE_TIME;
		// zone offset missing, field should be not available
		a = f.parse("2001-02-03T04:05:06");
		Assert.assertFalse(a.isSupported(ChronoField.OFFSET_SECONDS));
		try{
			a.get(ChronoField.OFFSET_SECONDS);
			Assert.fail("Expected exception not thrown");
		}catch( UnsupportedTemporalTypeException e ){
			// expected outcome
		}
		// zero zone offset, field should be available
		a = f.parse("2001-02-03T04:05:06Z");
		Assert.assertEquals(0, a.get(ChronoField.OFFSET_SECONDS));
		a = f.parse("2001-02-03T04:05:06+00:00");
		Assert.assertEquals(0, a.get(ChronoField.OFFSET_SECONDS));
		a = f.parse("2001-02-03T04:05"); // seconds can be omitted
		// test the partial timestamp formatter
		f = DateTimeFormatter.ofPattern("u[-M[-d['T'H[:m[:s[.S]]][X]]]]");
	}

	@Test
	public void verifyParsingIncompleteIsoTimestamp() throws ParseException{
		DateTimeAccuracy a;
		a = DateTimeAccuracy.parsePartialIso8601("2001");
		assertEquals(ChronoUnit.YEARS, a.getAccuracy());
		a = DateTimeAccuracy.parsePartialIso8601("2001-02");
		assertEquals(ChronoUnit.MONTHS, a.getAccuracy());
		a = DateTimeAccuracy.parsePartialIso8601("2001-02-03");
		assertEquals(ChronoUnit.DAYS, a.getAccuracy());
		a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04");
		assertEquals(ChronoUnit.HOURS, a.getAccuracy());
		a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04:05");
		assertEquals(ChronoUnit.MINUTES, a.getAccuracy());
		a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04:05:06");
		assertEquals(ChronoUnit.SECONDS, a.getAccuracy());
		a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04:05:06.789");
		assertEquals(ChronoUnit.SECONDS, a.getAccuracy());
		// verify zone offset
		// for second accuracy
		a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04:05:06+0800");
		assertEquals(ChronoUnit.SECONDS, a.getAccuracy());
		// zone offset calculation
		assertEquals(DateTimeAccuracy.parsePartialIso8601("2001-02-02T20:05:06Z"), a);
		
		a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04+0800");
		assertEquals(ChronoUnit.HOURS, a.getAccuracy());
		a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04Z");
		assertEquals(ChronoUnit.HOURS, a.getAccuracy());
		a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04:05+0800");
		assertEquals(ChronoUnit.MINUTES, a.getAccuracy());
		
	}
	@Test
	public void testFormatExceedsText(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.u[ H[:m[:s]]]");
		formatter.withResolverStyle(ResolverStyle.STRICT);

		DateTimeAccuracy a;
		a = DateTimeAccuracy.parse(formatter, "01.02.2003",  ZoneOffset.UTC);
		Assert.assertEquals(ChronoUnit.DAYS,a.getAccuracy());
		
		a = DateTimeAccuracy.parse(formatter, "01.02.2003 13",  ZoneOffset.UTC);
		Assert.assertEquals(ChronoUnit.HOURS,a.getAccuracy());

		a = DateTimeAccuracy.parse(formatter, "01.02.2003 13:14", ZoneOffset.UTC);
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
		DateTimeAccuracy a = DateTimeAccuracy.parsePartialIso8601("2003-02-01T04:05:06+0100");
		// make sure the date is adjusted to UTC
		TemporalAccessor ac = a.toInstantMin().atOffset(ZoneOffset.UTC);
		Assert.assertEquals(3, ac.get(ChronoField.HOUR_OF_DAY));
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
		DateTimeAccuracy a = DateTimeAccuracy.parse(formatter, "2003" ,ZoneOffset.UTC);
		Assert.assertEquals(ChronoUnit.YEARS, a.getAccuracy());
		// next two calls should throw exceptions
		try{
			DateTimeAccuracy.parse(formatter, "+", ZoneOffset.UTC);
			Assert.fail("Exception unexpected input");
		}catch( DateTimeParseException e ){
		}
		try{
			DateTimeAccuracy.parse(formatter, "2003+", ZoneOffset.UTC);
			Assert.fail("Exception expected for unparsed text at end of input");
		}catch( DateTimeParseException e ){
		}
	}
	@Test
	public void verifyParsingWithLocalZone() throws ParseException{
		DateTimeAccuracy a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04", ZoneId.of("Asia/Shanghai"));
		// date should be treated as if it had a +08:00 offset
		assertEquals("2001-02-02T20Z", a.toPartialIso8601(ZoneId.of("UTC")));
	}

	@Test
	public void verifyComparison() throws ParseException{
		ZoneId zone = ZoneOffset.UTC.normalized();
		DateTimeAccuracy a = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04", zone);
		DateTimeAccuracy b = DateTimeAccuracy.parsePartialIso8601("2001-02-03T04:00", zone);
		// instant min are equal
		Assert.assertEquals(a.toInstantMin(), b.toInstantMin());
		// yet a and b are not equal
		Assert.assertNotEquals(a, b);
		// defined order
		Assert.assertTrue(a.compareTo(b) > 0);
		Assert.assertTrue(b.compareTo(a) < 0);
	}
	// TODO: further tests
}
