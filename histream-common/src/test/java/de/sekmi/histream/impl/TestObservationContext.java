package de.sekmi.histream.impl;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;

public class TestObservationContext {

	public static ObservationImpl createObservation() {
		ObservationImpl o = new ObservationImpl();
		o.conceptId = "C0";
		try {
			o.startTime = DateTimeAccuracy.parsePartialIso8601("2015-01-01", ZoneOffset.UTC.normalized());
		} catch (ParseException e) {
			throw new AssertionError("Unable to parse date");
		}
		// string value
		o.setValue(new StringValue("strval"));
		o.setSource(new ExternalSourceImpl("source1",Instant.parse("2000-01-01T00:00:00Z")));
		return o;
	}
	@Test
	public void testRemoveContext(){
		ObservationImpl o = createObservation();
		o.removeContext(null, new ExternalSourceImpl("source1", Instant.now()));
		Assert.assertNull(o.getSource().getSourceId());
		Assert.assertNotNull(o.getSource().getSourceTimestamp());
		
		o = createObservation();
		o.removeContext(o.getStartTime(), new ExternalSourceImpl("source1", Instant.parse("2000-01-01T00:00:00Z")));
		Assert.assertNull(o.getSource());
		Assert.assertNull(o.getStartTime());
	}
	
	@Test
	public void testFillContext(){
		ObservationImpl o = createObservation();
		// TODO implement test
	}

	@Test
	public void testRemoveAndSetContext() {
		
	}


}
