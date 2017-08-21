package de.sekmi.histream.xml;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.time.ZoneId;

public class TestDateTimeAccuracyAdapter {

	@Test
	public void verifyParseAndFormat() throws ParseException{
		DateTimeAccuracyAdapter a = new DateTimeAccuracyAdapter();
		String[] testValues = new String[]{"2001","2001-02-03","2001-02-03T04"};
		for( int i=0; i<testValues.length; i++ ){
			assertEquals(testValues[i],a.marshal(a.unmarshal(testValues[i])));			
		}		
	}
	@Test
	public void verifyParseAndFormatWithZoneOffset() throws ParseException{
		DateTimeAccuracyAdapter a = new DateTimeAccuracyAdapter();
		a.setZoneId(ZoneId.of("Asia/Shanghai"));
		String[] testValues = new String[]{"2001","2001-02-03","2001-02-03T04+0800"};
		for( int i=0; i<testValues.length; i++ ){
			assertEquals(testValues[i],a.marshal(a.unmarshal(testValues[i])));			
		}		
		a.setZoneId(ZoneId.of("UTC"));
		testValues = new String[]{"2001","2001-02-03","2001-02-03T04Z"};
		for( int i=0; i<testValues.length; i++ ){
			assertEquals(testValues[i],a.marshal(a.unmarshal(testValues[i])));			
		}		
	}
}
