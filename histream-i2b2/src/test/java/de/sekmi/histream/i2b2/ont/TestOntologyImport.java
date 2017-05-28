package de.sekmi.histream.i2b2.ont;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.i2b2.sql.VirtualConnection;

public class TestOntologyImport {
	VirtualConnection dbcData;
	VirtualConnection dbcMeta;
	Import imp;
	
	@Before
	public void prepareVirtualConnections() throws SQLException{
		dbcData = new VirtualConnection( a -> {} );
		dbcMeta = new VirtualConnection( a -> {} );
		Map<String, String> props = new HashMap<>();
		// set preferences
		props.put("meta.basepath", "\\i2b2\\");
		props.put("meta.sourcesystem_cd", "test");
		props.put("meta.table", "i2b2metadata.i2b2");
		props.put("meta.access", "i2b2metadata.table_access");
		props.put("data.concept.table", "i2b2crcdata.concept_dimension");
		props.put("data.modifier.table", "i2b2crcdata.modifier_dimension");
		props.put("ont.language", "en");
		imp = new Import(dbcMeta, dbcData, props);
	}

	@After
	public void closeVirtualConnections() throws SQLException{
		imp.close();
//		dbcData.close();
//		dbcMeta.close();
	}
	@Test
	public void verifyReadablePath(){
		String path = "\\i2b2\\asdf:First\\k23kskd:Second\\";
		String readable = imp.readableConceptPath(path);
		// there should be no prefixes
		Assert.assertEquals(-1, readable.indexOf(':'));
		// print for debugging
		System.out.println(readable);

		// second test with url prefixes
		path = "\\i2b2\\http://lala/xyz#First\\asdf:Second";
		String readable2 = imp.readableConceptPath(path);
		System.out.println(readable);
		Assert.assertEquals(readable, readable2);
	}
}
