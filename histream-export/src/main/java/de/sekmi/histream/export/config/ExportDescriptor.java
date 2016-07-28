package de.sekmi.histream.export.config;

import java.io.InputStream;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Source;

/**
 * Export descriptor. Usually, this is specified
 * by an XML document and parsed via {@link #parse(Source)}
 * or using JAXB.
 * 
 * @author R.W.Majeed
 *
 */
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


	public VisitTable getVisitTable() {
		return visit;
	}
	public EavTable[] getEAVTables(){
		return tables;
	}
	/**
	 * Parse an XML document containing the
	 * export descriptor data.
	 * 
	 * @param source XML source
	 * @return export descriptor instance
	 */
	public static ExportDescriptor parse(Source source){
		return JAXB.unmarshal(source, ExportDescriptor.class);
	}
	/**
	 * Parse an XML document containing the
	 * export descriptor data.
	 * @param xml XML input stream
	 * @return export descriptor instance
	 */
	public static ExportDescriptor parse(InputStream xml){
		return JAXB.unmarshal(xml, ExportDescriptor.class);		
	}

	/**
	 * Get all concepts used for the export. This
	 * may contain duplicate concepts.
	 * 
	 * @return all concepts
	 */
	public Iterable<Concept> allConcepts(){
		return concepts.allConcepts();
	}

}
