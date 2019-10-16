package de.sekmi.histream.impl;

import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.ext.ExternalSourceType;

public class TestMeta {

	@Test
	public void writeAndReadSource() {
		Meta m = new Meta();
		Instant now = Instant.now();
		ExternalSourceType t = new ExternalSourceImpl("lala", now);
		m.setSource(t);
		ExternalSourceType t2 = m.getSource();
		Assert.assertEquals(t, t2);
	}
}
