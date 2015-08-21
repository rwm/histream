package de.sekmi.histream;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
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
	public void testFormatExceedsText(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.u[ H[:m[:s]]]");
		formatter.withResolverStyle(ResolverStyle.STRICT);
		DateTimeFormatterBuilder b = new DateTimeFormatterBuilder();
		DateTimeAccuracy a;
		a = DateTimeAccuracy.parse(formatter, "01.02.2003");
		Assert.assertEquals(ChronoUnit.DAYS,a.getAccuracy());
		
		a = DateTimeAccuracy.parse(formatter, "01.02.2003 13");
		//Assert.assertEquals(ChronoUnit.HOURS,a.getAccuracy());
		//will have second resolution (implicit)
		// TODO correct accuracy
	}
	// TODO: further tests
}
