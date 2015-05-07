package de.sekmi.histream.impl;

import java.io.FileReader;
import java.sql.SQLException;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.sekmi.histream.i2b2.I2b2Inserter;
import de.sekmi.histream.i2b2.I2b2Visit;

public class TestInsertXmlToI2b2 {
	TestPostgresVisitStore visitStore;
	TestPostgresPatientStore patientStore;
	private static final String postgresHost = "localhost";
	private static final int postgresPort = 15432;
	
	private void load()throws Exception{
		
		visitStore = new TestPostgresVisitStore();
		visitStore.open(postgresHost, postgresPort);
		//visitStore.getStore().deleteWhereSourceId("test");
		
		patientStore = new TestPostgresPatientStore();
		patientStore.open(postgresHost, postgresPort);
		//patientStore.getStore().deleteWhereSourceId("test");
		
		ObservationFactoryImpl factory = new ObservationFactoryImpl();
		factory.registerExtension(patientStore.getStore());
		factory.registerExtension(visitStore.getStore());
		XMLReader reader = XMLReaderFactory.createXMLReader();
		SAXObservationProvider provider = new SAXObservationProvider(factory);
		I2b2Inserter inserter = new I2b2Inserter();
		inserter.open();
		// delete data
		//inserter.purgeSource("test");
		
		provider.beforeFacts(v -> {
			try{
				inserter.purgeVisit(((I2b2Visit)v).getNum());
			}catch( SQLException e ){
				System.err.println("Unable to delete facts for visit: "+v);
			}
		});
		

		// load instance_num presets
		visitStore.getStore().loadMaxInstanceNums();

		provider.setHandler(inserter);
		reader.setContentHandler(provider);
		reader.parse(new InputSource(new FileReader("src/test/resources/dwh-eav.xml")));
		inserter.close();
		visitStore.close();
		patientStore.close();

	}
	
	public static void main(String args[]) throws Exception{
		TestInsertXmlToI2b2 t = new TestInsertXmlToI2b2();
		t.load();
	}
}
