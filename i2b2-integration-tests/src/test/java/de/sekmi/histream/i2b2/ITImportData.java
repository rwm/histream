package de.sekmi.histream.i2b2;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.junit.Test;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.etl.ETLObservationSupplier;
import de.sekmi.histream.impl.ObservationFactoryImpl;
import de.sekmi.histream.io.Streams;

public class ITImportData {

	@Test
	public void importData() throws Exception{
		Properties i2b2 = new Properties();
		try( InputStream in = getClass().getResourceAsStream("/i2b2.properties") ){
			i2b2.load(in);
		}
		PostgresPatientStore patients = new PostgresPatientStore((Map)i2b2);
		PostgresVisitStore visits = new PostgresVisitStore((Map)i2b2);
		I2b2Inserter inserter = new I2b2Inserter((Map)i2b2);
		ObservationFactory factory = new ObservationFactoryImpl();
		factory.registerExtension(patients);
		factory.registerExtension(visits);

		ObservationSupplier data = ETLObservationSupplier.load(ETLObservationSupplier.class.getResource("/test-1-datasource.xml"), factory);
		inserter.setMeta(ObservationSupplier.META_ETL_STRATEGY, data.getMeta(ObservationSupplier.META_ETL_STRATEGY));
		Supplier<Observation> source = data;
		// XXX test transformation later
		//source = new PullTransformer(source, mapping);
		try{
			Streams.transfer(source, inserter);
		}catch( UncheckedIOException e ){
			// unwrap unchecked IO exception
			throw e.getCause();
		}finally{
			data.close();
			inserter.close();
			patients.close();
			visits.close();
		}
	}
}
