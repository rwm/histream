package de.sekmi.histream.etl;

import java.net.URL;
import java.nio.file.Paths;

import org.junit.Test;

import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.io.GroupedXMLWriter;
import de.sekmi.histream.io.Streams;

public class OtherSources {

	@Test
	public void testSource1() throws Exception{
		URL url = Paths.get("../../../2015-06 DZL/Datenquellen/Biobanken/Marburg (Cosyconet)/datasource.xml").toUri().toURL();
		ETLObservationSupplier os = ETLObservationSupplier.load(url);
		GroupedXMLWriter w = new GroupedXMLWriter(System.out);
		Meta.transfer(os, w);
		Streams.transfer(os, w);
		os.close();
		w.close();
	}
	@Test
	public void testSource2() throws Exception{
		URL url = Paths.get("../../../2015-06 DZL/Datenquellen/Biobanken/Gauting/datasource.xml").toUri().toURL();
		ETLObservationSupplier os = ETLObservationSupplier.load(url);
		GroupedXMLWriter w = new GroupedXMLWriter(System.out);
		Meta.transfer(os, w);
		Streams.transfer(os, w);
		os.close();
		w.close();
	}
}
