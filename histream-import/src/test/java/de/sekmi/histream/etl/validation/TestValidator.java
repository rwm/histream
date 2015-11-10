package de.sekmi.histream.etl.validation;

import org.junit.Test;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.etl.ETLObservationSupplier;
import de.sekmi.histream.io.Streams;

public class TestValidator {

	
	@Test
	public void validateData1() throws Exception{
		try( ObservationSupplier os = ETLObservationSupplier.load(getClass().getResource("/data/test-1-datasource.xml")) ){
			Validator v = new Validator();
			v.setErrorHandler(e -> {throw new RuntimeException(e);});
			Streams.transfer(os, v);			
		}
	}
}
