package de.sekmi.histream.io;

import org.junit.Test;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.io.transform.PullTransformer;
import de.sekmi.histream.io.transform.Transformation;


public class TransformerTest {
	@Test
	public void testPullTransformerIdentity() throws Exception{
//		FileObservationProviderTest f = new FileObservationProviderTest();
//		f.initializeObservationFactory();
//
		Transformation t = Transformation.Identity;
//		InputStream in = new FileInputStream("examples/dwh-flat.txt");
		ObservationSupplier sup = TestXMLReader.getResourceReader("/dwh.xml", null);
		
		PullTransformer p = new PullTransformer(sup, t);

		// validate content after identity transformation
//		f.initializeHandler();
//		f.validateExample(p);
//		f.closeHandler();

		sup.close();
//		in.close();
	}
}
