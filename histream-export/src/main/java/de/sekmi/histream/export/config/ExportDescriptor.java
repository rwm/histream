package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="export")
@XmlAccessorType(XmlAccessType.NONE)
public class ExportDescriptor {

	@XmlElement
	Concepts concepts;
	
	@XmlElement(name="patient-table")
	PatientTable patient;
	
	@XmlElement(name="visit-table")
	VisitTable visit;
	
	@XmlElement(name="eav-table")
	EavTable[] tables;


	public PatientTable getPatientTable(){
		return patient;
	}


	public AbstractTable getVisitTable() {
		return visit;
	}
}
