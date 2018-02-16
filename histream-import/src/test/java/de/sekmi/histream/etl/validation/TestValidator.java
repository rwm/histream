package de.sekmi.histream.etl.validation;

import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.etl.ETLObservationSupplier;
import de.sekmi.histream.etl.config.DataSource;
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
			for( int i=0; i<5; i++ ){
				Assert.assertNull("Additional calls to get() should return null", os.get());
			}			
		}
	}
	@Test
	public void validateData2() throws Exception{
		try( ObservationSupplier os = ETLObservationSupplier.load(getClass().getResource("/data/test-2-datasource.xml")) ){
			Validator v = new Validator(true,true);
			v.setErrorHandler(e -> {throw new RuntimeException(e);});
			Streams.transfer(os, v);
		}catch( RuntimeException e ){
			if( e.getCause() instanceof DuplicatePatientException ){
				// expected behaviour
				return;
			}else{
				// unexpected exceptoin
				throw e;
			}
		}
		Assert.fail("Exception expected");
	}
	@Test
	public void validateData3() throws Exception{
		// empty patient table
		try( ObservationSupplier os = ETLObservationSupplier.load(getClass().getResource("/data/test-3-datasource.xml")) ){
			Validator v = new Validator(true,true);
			v.setErrorHandler(e -> {throw new RuntimeException(e);});
			Streams.transfer(os, v);
		}
	}
	@Test
	public void validateData4() throws Exception{
		// duplicate concepts
		try( ObservationSupplier os = ETLObservationSupplier.load(getClass().getResource("/data/test-4-datasource.xml")) ){
			Validator v = new Validator(true,true);
			v.setErrorHandler(e -> {throw new RuntimeException(e);});
			Streams.transfer(os, v);
		}catch( RuntimeException e ){
			if( e.getCause() instanceof DuplicateConceptException ){
				// expected behaviour
				return;
			}else{
				// unexpected exceptoin
				throw e;
			}
		}
		Assert.fail("Exception expected");
	}
	@Test
	public void validateData4WithDuplicateFilter() throws Exception{
		// duplicate concepts
		try( ObservationSupplier os = ETLObservationSupplier.load(getClass().getResource("/data/test-4-datasource2.xml")) ){
			Validator v = new Validator(true,true);
			v.setErrorHandler(e -> {throw new RuntimeException(e);});
			Streams.transfer(os, v);
		}
		// no duplicate concept exception should occur
	}
}
