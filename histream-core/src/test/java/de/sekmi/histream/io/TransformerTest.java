package de.sekmi.histream.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;


public class TransformerTest {
	
	@SuppressWarnings("resource")
	@Test
	public void testPullTransformerIdentity() throws FileNotFoundException, IOException{
		FileObservationProviderTest f = new FileObservationProviderTest();
		f.initializeObservationFactory();

		Transformation t = Transformation.Identity;
		try( FlatObservationSupplier sup = new FlatObservationSupplier(f.getFactory(), new FileInputStream("examples/dwh-flat.txt")) ){
			PullTransformer p = new PullTransformer(sup, t);
			
			// validate content after identity transformation
			f.initializeHandler();
			f.validateExample(p);
			f.closeHandler();
		}
	}
}
