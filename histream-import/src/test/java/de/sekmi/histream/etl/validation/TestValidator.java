package de.sekmi.histream.etl.validation;

import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.etl.ETLObservationSupplier;
import de.sekmi.histream.io.Streams;

public class TestValidator {

	
	/**
	 * Should validate successfully without exception
	 * @throws Exception should not occur
	 */
	@Test
	public void validateData1() throws Exception{
		try( ObservationSupplier os = ETLObservationSupplier.load(getClass().getResource("/data/test-1-datasource.xml")) ){
			Validator v = new Validator(true,true);
			v.setErrorHandler(e -> {throw new RuntimeException(e);});
			Streams.transfer(os, v);
		}
	}
	@Test
	public void validateData2() throws Exception{
		try( ObservationSupplier os = ETLObservationSupplier.load(getClass().getResource("/data/test-2-datasource.xml")) ){
			Validator v = new Validator(true,true);
			v.setErrorHandler(e -> {throw new RuntimeException(e);});
			Streams.transfer(os, v);
		}catch( RuntimeException e ){
			Assert.assertTrue(e.getCause() instanceof DuplicatePatientException);
			return;
		}
		Assert.fail("Exception expected");
	}
}
