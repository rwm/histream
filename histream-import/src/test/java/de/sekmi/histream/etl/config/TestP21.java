package de.sekmi.histream.etl.config;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.etl.XMLExport;

public class TestP21 {

	public static void main(String[] args) throws IOException, ParseException, XMLStreamException {
		
		XMLExport.descriptorToXML(Class.class.getResource("/data/test-p21-datasource.xml"), System.out);
	}
}
