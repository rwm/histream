package de.sekmi.histream.scripting;

import org.junit.Assert;
import org.junit.Test;

public class TestEncounterInferenceFactory {

	@Test
	public void testMetaInfo() throws Exception{
		JSEncounterInferenceFactory f = new JSEncounterInferenceFactory();
		f.addScript(getClass().getResource("/encounter-inference-1.js"), "UTF-8");
		Assert.assertTrue( f.canInfer("ASDF") );
		Assert.assertFalse( f.canInfer("XYZ") );
		
	}
}
