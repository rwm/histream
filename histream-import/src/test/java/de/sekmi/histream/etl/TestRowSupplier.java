package de.sekmi.histream.etl;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class TestRowSupplier {
	
	@Test
	public void testLoadRows() throws IOException{
		try( FileRowSupplier r = new FileRowSupplier(getClass().getResource("/data/test-1-patients.txt"), "\t") ){
			String[] h = r.getHeaders();
			Assert.assertEquals("patid", h[0]);
			Assert.assertEquals("nachname", h[2]);
			Object[] f = r.get();
			Assert.assertEquals("n1", f[2]);			
		}
		
	}

}
