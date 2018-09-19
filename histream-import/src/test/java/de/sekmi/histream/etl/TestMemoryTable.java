package de.sekmi.histream.etl;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Test;
import static org.junit.Assert.*;
public class TestMemoryTable {

	@Test
	public void verifyOriginalOrderWithoutModification() throws IOException {
		FileRowSupplier visits = new FileRowSupplier(getClass().getResource("/data/p21khg/ICD.csv"), ";", Charset.forName("ASCII"));
		MemoryTable mt = new MemoryTable(visits);
		assertEquals(15, mt.getRowCount());
		// verify that the table is previously unsorted
		Object[] r = mt.get();
		// first record on line 2 (first line were headers)
		assertEquals(2, mt.getLineNumber());
		assertEquals("KH-internes-Kennzeichen", mt.getHeaders()[3]);
		// visit 1
		assertEquals("1", r[3]);
		// visit 2
		assertEquals("2", mt.get()[3]);
		mt.close();
	}
	@Test
	public void verifySortSingleColumn() throws IOException {
		FileRowSupplier visits = new FileRowSupplier(getClass().getResource("/data/p21khg/ICD.csv"), ";", Charset.forName("ASCII"));
		MemoryTable mt = new MemoryTable(visits);
		// sorting by single column
		mt.sort(new int[] {3});
		// now we should have two consecutive rows with visit 1
		assertEquals("1", mt.get()[3]);
		assertEquals("1", mt.get()[3]);
		assertEquals("11", mt.get()[3]);
		assertEquals("2", mt.get()[3]);
		mt.close();
	}
	@Test
	public void verifyUniqueSingleColumn() throws IOException {
		FileRowSupplier visits = new FileRowSupplier(getClass().getResource("/data/p21khg/ICD.csv"), ";", Charset.forName("ASCII"));
		MemoryTable mt = new MemoryTable(visits);
		// sorting by single column
		mt.sort(new int[] {3});
		mt.unique(new int[] {3});
		// now we should have only one row with visit 1
		assertEquals("1", mt.get()[3]);
		assertEquals("11", mt.get()[3]);
		assertEquals("2", mt.get()[3]);
		assertEquals("3", mt.get()[3]);
		assertEquals("4", mt.get()[3]);
		assertEquals("5", mt.get()[3]);
		assertEquals("6", mt.get()[3]);
		assertEquals("7", mt.get()[3]);
		// in total without duplicates, there should be 11 rows 
		assertEquals(8, mt.getRowCount());
		mt.close();
	}
}
