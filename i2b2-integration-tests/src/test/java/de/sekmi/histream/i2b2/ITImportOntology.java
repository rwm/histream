package de.sekmi.histream.i2b2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import de.sekmi.histream.i2b2.ont.Import;
import de.sekmi.histream.ontology.skos.Store;

public class ITImportOntology {

	@Test
	public void importOntology() throws Exception{
		Properties i2b2 = new Properties();
		try( InputStream in = getClass().getResourceAsStream("/i2b2.properties") ){
			i2b2.load(in);
		}
		Store store = new Store(new File("../histream-skos/examples/test-ontology.ttl"));
		try( Import i = new Import((Map)i2b2) ){
			i.setOntology(store);
			i.processOntology();
		}
		store.close();
	}
}
