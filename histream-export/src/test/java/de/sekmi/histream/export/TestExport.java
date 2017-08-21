package de.sekmi.histream.export;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.export.ExceptionCausingWriter.WhenToThrow;
import de.sekmi.histream.export.ExceptionCausingWriter.WhereToThrow;
import de.sekmi.histream.export.config.ExportDescriptor;
import de.sekmi.histream.io.FileObservationProviderTest;

// TODO expectExceptionForOverlappingGroups
// TODO expectExceptionForDuplicateConcepts

public class TestExport {

	private ExportDescriptor descriptor;
	private TableExport export;

	@Before
	public void initialize() throws Exception{
		try( InputStream in = getClass().getResourceAsStream("/export1.xml") ){
			descriptor = ExportDescriptor.parse(in);
		}
		export = new TableExport(descriptor);

	}
	@Test
	public void verifyExport() throws Exception{
		MemoryExportWriter m = new MemoryExportWriter();
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		try( ObservationSupplier s = t.getExampleSupplier() ){
			export.setZoneId(ZoneId.of("Asia/Shanghai"));
			export.export(s, m);
			export.setZoneId(null);
		}
		// verify class lookup
		Assert.assertEquals("T:type:str", m.get(MemoryExportWriter.VISIT_TABLE, "byclass", 0));
		m.dump();
		// verify eav table
		Assert.assertEquals(6, m.rowCount("eavtabletest"));
		Assert.assertEquals("T:date:month", m.get("eavtabletest", "code", 4));
		
		// TODO something wrong with namespaces in xpath/dom
	}

	/**
	 * IOExceptions occurring during stream operations should
	 * be unwrapped and directly passed through to the export
	 * call.
	 * 
	 * @throws Exception unexpected failure
	 */
	@Test
	public void expectIOExceptionPassThrough() throws Exception{
		ExceptionCausingWriter w = new ExceptionCausingWriter(WhereToThrow.VisitTable, WhenToThrow.CloseTable);
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		try( ObservationSupplier s = t.getExampleSupplier() ){
			export.export(s, w);
			Assert.fail("IOException should have been thrown");
		}catch( IOException e ){
			// success
		}
	}
}
