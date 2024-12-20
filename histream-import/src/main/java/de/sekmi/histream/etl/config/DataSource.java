package de.sekmi.histream.etl.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import de.sekmi.histream.etl.filter.PostProcessingFilter;

/**
 * Data source configuration.
 * This is the XML root element which can be loaded 
 * via {@code JAXB.unmarshal(in, DataSource.class);}
 * <p>
 * For relative URLs to work, {@link Meta#setLocation(java.net.URL)} must be called to set
 * the location of the data source description.
 * 
 * @see JAXB#unmarshal(java.io.File, Class)
 * @author R.W.Majeed
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
	
	@XmlElement(name="post-processing", required=false)
	PostProcessing postProcessing;
	/**
	 * Scripts to execute for each visit. A script
	 * can add or delete facts for the visit. If scripts
	 * are provided, all facts need to be read for each visit
	 * before the scripts are executed.
	 */
	@XmlElement(name="script", required=false)
	Script[] scripts;
	
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
	/**
	 * Load the datasource configuration from the given URL.
	 * @param configuration configuration URL
	 * @return data source object
	 * @throws IOException IO error
	 * @throws NullPointerException /datasource or /datasource/meta missing
	 */
	public static DataSource load(URL configuration) throws IOException, NullPointerException{
		URLConnection conn = configuration.openConnection();
		conn.connect();
		DataSource ds;
		try( InputStream in = conn.getInputStream() ){
			ds = JAXB.unmarshal(configuration, DataSource.class);			
		}
		Objects.requireNonNull(ds, "JAXB unmarshall returned null for URL "+configuration);
		Objects.requireNonNull(ds.getMeta(), "/datasource/meta element missing");
		ds.getMeta().setLastModified(conn.getLastModified());
		ds.getMeta().setLocation(configuration);
		return ds;
	}

	public PostProcessingFilter[] getPostProcessingFilters(){
		if( postProcessing == null ){
			return new PostProcessingFilter[]{};
		}
		return postProcessing.filter;
	}
	/**
	 * If scripts are present, an instance of {@link ScriptProcessingQueue}
	 * is returned. Otherwise an instance of {@link FactGroupingQueue}.
	 * @param factory factory
	 * @return fact queue
	 * @throws IOException error
	 */
//	public FactGroupingQueue createFactQueue(ObservationFactory factory) throws IOException{
//		if( true ){
//			// TODO debug problems with visitpostprocessorqueue
//			return new VisitPostProcessorQueue() {
//				@Override
//				protected void postProcessVisit() {
//				}
//			};
//		}
//		if( scripts == null || scripts.length == 0 ){
//			return new FactGroupingQueue();
//		}else{
//			return new ScriptProcessingQueue(scripts, meta, factory);
//		}
//	}
}
