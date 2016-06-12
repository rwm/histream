package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="export")
public class ExportDescriptor {

	@XmlElement
	PatientTable patient;
	
	@XmlElement
	VisitTable visit;
	
	@XmlElement(name="table")
	FactTable[] tables;
}
