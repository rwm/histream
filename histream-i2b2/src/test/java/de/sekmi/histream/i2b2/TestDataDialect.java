package de.sekmi.histream.i2b2;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.*;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;
public class TestDataDialect {

	@Test
	public void verifySqlTimestampConversions(){
		DataDialect dialect = new DataDialect();
		dialect.setTimeZone(ZoneId.of("Asia/Shanghai"));
		
		LocalDateTime local = LocalDateTime.of(2001,2,3,4,5);
		System.out.println(local.toString());
		Timestamp ts = Timestamp.valueOf(local);
		System.out.println(ts.toInstant());

		Instant inst = Instant.parse("2001-02-03T04:05:06Z");
		DateTimeAccuracy da = new DateTimeAccuracy(inst);
		System.out.println(inst);
		
		ts = dialect.encodeInstant(inst);
		assertEquals(ts, dialect.encodeInstantPartial(da));
		System.out.println(ts.toInstant());


		Instant b = dialect.decodeInstant(ts);
		System.out.println(b);
		assertEquals(inst, b);
	}
}
