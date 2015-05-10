package de.sekmi.histream.impl;

import java.io.FileInputStream;



import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import de.sekmi.histream.i2b2.I2b2Inserter;
import de.sekmi.histream.i2b2.I2b2Visit;
import de.sekmi.histream.io.SAXObservationProvider;

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
		SAXObservationProvider provider = new SAXObservationProvider();
		provider.setObservationFactory(factory);
		
		Map<String,String> props = new HashMap<>();
		props.put("user", "i2b2demodata");
		props.put("host", "localhost");
		props.put("database", "i2b2");
		props.put("port", "15432");
		props.put("password", "");
		props.put("nullProvider", "LCS-I2B2:PROVIDERS");

		I2b2Inserter inserter = new I2b2Inserter(props);
		// delete data
		//inserter.purgeSource("test");

		// load instance_num presets
		visitStore.getStore().loadMaxInstanceNums();

		// find distinct visits and delete each before inserting
		provider.setHandler(new DistinctExtensionFilter<I2b2Visit>(inserter, I2b2Visit.class, v -> {
			try{
				inserter.purgeVisit(((I2b2Visit)v).getNum());
			}catch( SQLException e ){
				System.err.println("Unable to delete facts for visit: "+v);
			}			
		} ));
		
		provider.parse(new FileInputStream("src/test/resources/dwh-eav.xml"));
		inserter.close();
		visitStore.close();
		patientStore.close();

	}
	
	public static void main(String args[]) throws Exception{
		TestInsertXmlToI2b2 t = new TestInsertXmlToI2b2();
		t.load();
	}
}
