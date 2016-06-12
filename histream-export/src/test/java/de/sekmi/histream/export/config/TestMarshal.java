package de.sekmi.histream.export.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

public class TestMarshal {

	@Test
	public void verifyMarshal() throws JAXBException{
		ExportDescriptor e = new ExportDescriptor();
		e.patient = new PatientTable();
		e.patient.columns = new AbstractColumn[2];
		e.patient.columns[0] = new IdColumn();
		e.patient.columns[0].header = "pid";
		e.patient.columns[1] = new SequenceColumn("seq1");
		e.patient.columns[1].header = "seq";
		
		JAXBContext j = JAXBContext.newInstance(ExportDescriptor.class);
		Marshaller m = j.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(e, System.out);
		
	}
}
