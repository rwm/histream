package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Column.class, StringColumn.class})
public class DataSource {
	@XmlElement
	Meta meta;
	
	@XmlElementWrapper(name="transformation")
	@XmlElement(name="xml-source")
	XmlSource[] xmlSources;

	@XmlElement(name="patient-table",required=true)
	PatientTable patientTable;
	
	@XmlElement(name="visit-table")
	VisitTable visitTable;
	
	@XmlElement(name="wide-table")
	WideTable[] wideTables;
	
	
	
}
