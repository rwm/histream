package de.sekmi.histream.i2b2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.*;
import org.junit.Test;

import de.sekmi.histream.DateTimeAccuracy;

public class TestDataDialect {

	@Test
	public void encodeDecodeInstant() {
		Instant inst = Instant.parse("2001-02-03T04:05:06Z");
		DataDialect dialect = new DataDialect();
		ZoneId tz = ZoneId.of("Asia/Shanghai");
		dialect.setTimeZone(tz);

		assertEquals(dialect.decodeInstant(dialect.encodeInstant(inst)), inst);
		
		Timestamp ts = dialect.encodeInstant(inst);
		
		assertEquals(dialect.encodeInstant(dialect.decodeInstant(ts)), ts);
	}
	@Test
	public void localDateTimeVerbatimOutput(){
		LocalDateTime local = LocalDateTime.of(2001,2,3,4,5);
		// local date time will be encoded as is, without offset
		assertEquals("2001-02-03T04:05", local.toString());
		// SQL timestamp will use the local timezone.
		// We should not use this method for conversion with explicit zones
		Timestamp ts = Timestamp.valueOf(local);
		System.out.println("Local time " + local + " to SQL Timestamp: "+ts.toInstant());
	}
	@Test
	public void verifySqlTimestampConversions(){
		DataDialect dialect = new DataDialect();
		ZoneId tz = ZoneId.of("Asia/Shanghai");
		dialect.setTimeZone(tz);
		Timestamp ts;

		Instant inst = Instant.parse("2001-02-03T04:05:06Z");
		DateTimeAccuracy da = new DateTimeAccuracy(inst);
		System.out.println(inst);
		
		System.out.println(dialect.encodeInstantPartial(da,tz));
		ts = dialect.encodeInstant(inst);
		// TODO endcodeInstantPartial works different than endcodeInstant
//		assertEquals(ts, dialect.encodeInstantPartial(da,tz));
//		System.out.println(ts.toInstant());


		Instant b = dialect.decodeInstant(ts);
		System.out.println(b);
		assertEquals(inst, b);
	}

	public static void main(String[] args) throws SQLException{
		// verify how timestamps get written to the database
		Connection c = new TestExtractor().getConnection();
		Statement s = c.createStatement();
		s.executeUpdate("DELETE FROM source_master WHERE source_cd='db_test'");
		s.close();
		PreparedStatement ps = c.prepareStatement("INSERT INTO source_master(source_cd, create_date) VALUES(?,?)");
		ps.setString(1, "db_test");
		Instant inst = Instant.parse("2001-08-03T04:05:06Z");
		ps.setTimestamp(2, Timestamp.from(inst));
		ps.executeUpdate();
		ps.close();
		//c.commit();
		c.close();
	}
}
