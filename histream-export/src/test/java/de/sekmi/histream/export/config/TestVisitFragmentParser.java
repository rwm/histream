package de.sekmi.histream.export.config;

import org.junit.Test;
import org.w3c.dom.Node;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.export.VisitFragmentSupplier;
import de.sekmi.histream.io.FileObservationProviderTest;
import de.sekmi.histream.xml.XMLUtils;

public class TestVisitFragmentParser {

	@Test
	public void verifyVisitFragmentContent() throws Exception{
		FileObservationProviderTest t = new FileObservationProviderTest();
		t.initializeObservationFactory();
		final Node visitNode = null;
		try( ObservationSupplier s = t.getExampleSupplier() ){
			VisitFragmentSupplier sup = new VisitFragmentSupplier(s);
			Node n = sup.get();
			//System.out.println(n.toString());
			XMLUtils.printDOM(n, System.out);
		}
	}
	
}
