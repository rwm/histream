package de.sekmi.histream.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.impl.SimplePatientExtension;
import de.sekmi.histream.impl.SimpleVisitExtension;

public class TestFileObservationSuppliers {
	private ObservationFactory factory;
	
	@Before
	public void prepareObservationFactory(){
		factory = new ObservationFactoryImpl();
		factory.registerExtension(new SimplePatientExtension());
		factory.registerExtension(new SimpleVisitExtension());
	}
	
	@Test
	public void verifyGroupedXmlSupplier() throws Exception{
		GroupedXMLProvider p = new GroupedXMLProvider(null);
		ObservationSupplier s = p.createSupplier(getClass().getResourceAsStream("/dwh.xml"), factory);
		Assert.assertTrue( s.stream().count() > 0 );
		s.close();
		p.close();
	}
	@Test
	public void verifyFlatSupplier() throws Exception{
		FlatProviderFactory p = new FlatProviderFactory(null);
		ObservationSupplier s = p.createSupplier(new File("examples/dwh-flat.txt"), factory);
		Assert.assertTrue( s.stream().count() > 0 );
		s.close();
		p.close();
	}

	public static void main(String[] args) throws Exception{
		TestFileObservationSuppliers test = new TestFileObservationSuppliers();
		test.prepareObservationFactory();
		
		GroupedXMLProvider p = new GroupedXMLProvider(null);
		try( InputStream in = new FileInputStream(args[0]) ){
			ObservationSupplier s = p.createSupplier(in, test.factory);
			Assert.assertTrue( s.stream().count() > 0 );
			s.close();
			p.close();		
		}
		
	}
}
