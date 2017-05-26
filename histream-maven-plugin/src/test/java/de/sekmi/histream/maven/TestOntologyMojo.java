package de.sekmi.histream.maven;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.maven.shared.model.fileset.FileSet;
import org.junit.Assert;
import org.junit.Test;

public class TestOntologyMojo {

	@Test
	public void expectSuccessfulExecution() throws Exception{
		URL ttl = getClass().getResource("/skos-aktin-cda.ttl");
		File source = new File(ttl.toURI());
		Assert.assertTrue(source.isFile());
		OntologyMojo mojo = new OntologyMojo();
		mojo.source = new FileSet();
		mojo.source.setDirectory(source.getParent());
		mojo.source.addInclude(source.getName());
		mojo.destination = new File("target/i2b2-aktin");
		mojo.properties = new Properties();
		mojo.overwrite = true;
		try( InputStream in = getClass().getResourceAsStream("/ontology.properties") ){
			mojo.properties.load(in);			
		}
				
		mojo.execute();
	}
	@Test
	public void writeComplexOntology() throws Exception{
		URL ttl = getClass().getResource("/test-ontology.ttl");
		File source = new File(ttl.toURI());
		Assert.assertTrue(source.isFile());
		OntologyMojo mojo = new OntologyMojo();
		mojo.source = new FileSet();
		mojo.source.setDirectory(source.getParent());
		mojo.source.addInclude(source.getName());
		mojo.destination = new File("target/i2b2-test");
		mojo.properties = new Properties();
		mojo.overwrite = true;
		try( InputStream in = getClass().getResourceAsStream("/ontology.properties") ){
			mojo.properties.load(in);			
		}
				
		mojo.execute();
	}
}
