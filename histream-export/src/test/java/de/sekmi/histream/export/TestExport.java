package de.sekmi.histream.export;

import javax.xml.bind.JAXB;

import org.junit.Test;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.export.config.ExportDescriptor;
import de.sekmi.histream.io.FileObservationProviderTest;

public class TestExport {

	@Test
	public void verifyExport() throws Exception{
		ExportDescriptor d = JAXB.unmarshal(getClass().getResourceAsStream("/export1.xml"), ExportDescriptor.class);
		MemoryExportWriter m = new MemoryExportWriter();
		TableExportFactory e = new TableExportFactory(d);
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		try( ObservationSupplier s = t.getExampleSupplier() ){
			e.export(s, m);
		}
		m.dump();
		// TODO something wrong with namespaces in xpath/dom
	}
}
