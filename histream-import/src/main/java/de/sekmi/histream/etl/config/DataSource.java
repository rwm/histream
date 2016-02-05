package de.sekmi.histream.etl.config;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Data source configuration.
 * This is the XML root element which can be loaded 
 * via {@code JAXB.unmarshal(in, DataSource.class);}
 * <p>
 * For relative URLs to work, {@link Meta#setLocation(java.net.URL)} must be called to set
 * the location of the data source description.
 * 
 * @see JAXB#unmarshal(java.io.File, Class)
 * @author Raphael
 *
 */
@XmlRootElement(name="datasource")
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
	
	@XmlElement(name="eav-table")
	EavTable[] eavTables;
	
	public Meta getMeta(){return meta;}
	
	public PatientTable getPatientTable(){
		return patientTable;
	}
	
	public VisitTable getVisitTable(){
		return visitTable;
	}
	
	public List<WideTable> getWideTables(){
		if( wideTables != null ){
			return Arrays.asList(wideTables);
		}else{
			return Arrays.asList();
		}
	}
	
	public List<EavTable> getEavTables(){
		if( eavTables != null ){
			return Arrays.asList(eavTables);
		}else{
			return Arrays.asList();
		}
	}
	
}
