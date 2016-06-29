package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="export")
public class ExportDescriptor {

	@XmlElement
	Concepts concepts;
	
	@XmlElement(name="patient-table")
	PatientTable patient;
	
	@XmlElement(name="visit-table")
	VisitTable visit;
	
	@XmlElement(name="eav-table")
	EavTable[] tables;
}
