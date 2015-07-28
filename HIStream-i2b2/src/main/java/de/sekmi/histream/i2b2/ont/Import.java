package de.sekmi.histream.i2b2.ont;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.sekmi.histream.Plugin;
import de.sekmi.histream.ontology.Concept;
import de.sekmi.histream.ontology.Ontology;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.ValueRestriction;

/**
 * Import ontology data into i2b2.
 * <p>
 * Use the methods in the following order: {@link #openDatabase(Map)}, {@link #loadOntology(Class, Map)}, {@link #processOntology()}, {@link #close
 *
 * @author Raphael
 *
 */
public class Import implements AutoCloseable{
	private static final Logger log = Logger.getLogger(Import.class.getName());
	private Ontology ontology;
	private Connection dbMeta;
	private Connection dbData;
	private Map<String,String> config;
	private Locale locale;
	
	private PreparedStatement insertMeta;
	private PreparedStatement insertAccess;
	private PreparedStatement insertConcept;
	
	private int insertMetaCount;
	private int insertAccessCount;
	private int insertConceptCount;
	
	private String sourceId;
	private String scheme;
	private Timestamp sourceTimestamp;
	
	/**
	 * Connect to the i2b2 database.
	 * The current implementation supports only Postgres databases for i2b2 versions >= 1.7.05(?)
	 * <p>
	 * Two connections are established: One for access to the metadata schema, the second for access to concept_dimension in the data schema.
	 * Connection arguments start with {@code meta.jdbc.} or {@code data.jdbc.}.
	 * 
	 * @param props connection parameters.
	 * Use {@code meta.sourcesystem_cd} for the source system.
	 * {@code jdbc.host}, {@code jdbc.port}, {@code jdbc.database} are used to construct the connect string. 
	 * Any other parameters starting with {@code jdbc.} are also passed to {@link DriverManager#getConnection(String, Properties)}.
	 * <p>
	 * More parameters: {@code ont.language} and {@code ont.scheme}.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Import(Map<String,String> props) throws ClassNotFoundException, SQLException{
		openDatabase(props);
	}
	
	private String getMetaTable(){return config.get("meta.table");}
	private String getAccessTable(){return config.get("meta.access");}
	private String getConceptTable(){return config.get("data.concept.table");}
	
	private void prepareStatements() throws SQLException{
		insertMeta = dbMeta.prepareStatement("INSERT INTO "+getMetaTable()+"(c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_basecode,c_metadataxml,c_facttablecolumn,c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,c_tooltip,m_applied_path,update_date,download_date,import_date,sourcesystem_cd)VALUES(?,?,?,?,?,?,?,'concept_cd','concept_dimension','concept_path','T','LIKE',?,?,?,current_timestamp,?,current_timestamp,?)");
		String access_table_name = getMetaTable();
		if( access_table_name.indexOf('.') >= 0 ){
			// name contains tablespace
			// find just the name
			access_table_name = access_table_name.substring(access_table_name.indexOf('.')+1);
		}
		insertAccess = dbMeta.prepareStatement("INSERT INTO "+getAccessTable()+"(c_table_cd,c_table_name,c_protected_access,c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_facttablecolumn,c_dimtablename,c_columnname,c_columndatatype,c_operator,c_dimcode,c_tooltip)VALUES(?,'"+access_table_name+"','N',?,?,?,?,?,'concept_cd','concept_dimension','concept_path','T','LIKE',?,?)");
		insertConcept = dbData.prepareStatement("INSERT INTO "+getConceptTable()+"(concept_path,concept_cd,name_char,update_date,download_date,import_date,sourcesystem_cd)VALUES(?,?,?,current_timestamp,?,current_timestamp,?)");
	}
	
	private void deleteFromDatabase() throws SQLException{
		PreparedStatement deleteOnt = dbMeta.prepareStatement("DELETE FROM "+getMetaTable()+" WHERE sourcesystem_cd=?");
		PreparedStatement deleteAccess = dbMeta.prepareStatement("DELETE FROM "+getAccessTable()+" WHERE c_table_cd LIKE ?");
		PreparedStatement deleteConcepts = dbData.prepareStatement("DELETE FROM "+getConceptTable()+" WHERE sourcesystem_cd=?");
		int count;
		
		deleteConcepts.setString(1, sourceId);
		count = deleteConcepts.executeUpdate();
		System.out.println("Deleted "+count+" rows from "+getConceptTable());
		deleteConcepts.close();

		deleteAccess.setString(1, sourceId+"%");
		count = deleteAccess.executeUpdate();
		System.out.println("Deleted "+count+" rows from "+getAccessTable());
		deleteAccess.close();

		deleteOnt.setString(1, sourceId);
		count = deleteOnt.executeUpdate();
		System.out.println("Deleted "+count+" rows from "+getMetaTable());
		deleteOnt.close();

	}
	
	public void processOntology() throws SQLException, OntologyException{
		deleteFromDatabase();

		// parse language for locale
		if( config.get("ont.language") == null ){
			locale = Locale.getDefault();
		}else{
			locale = Locale.forLanguageTag(config.get("ont.language"));
		}
		// parse scheme
		if( config.get("ont.language") != null ){
			this.scheme = config.get("ont.scheme");
		}
		
		// parse base path
		String base = config.get("meta.basepath");
		int base_level;
		if( base != null && !base.equals("\\") ){
			String[] baseParts = base.split("\\\\");
			if( baseParts.length < 2 || baseParts[0].length() != 0 || !base.endsWith("\\") )
				throw new IllegalArgumentException("meta.basepath must start and end with a backslash (\\)");
			base_level = baseParts.length - 1;
		}else{
			base = "\\";
			base_level = 0;
		}
		
		Concept[] concepts = ontology.getTopConcepts(this.scheme);


		for( Concept c : concepts ){
			insertMeta(base_level, base, c, true);
		}
		
		System.out.println("Inserted "+insertConceptCount+" rows to "+getConceptTable());
		System.out.println("Inserted "+insertAccessCount+" rows to "+getAccessTable());
		System.out.println("Inserted "+insertMetaCount+" rows to "+getMetaTable());
	}
	private void insertConceptDimension(String path, String name, String concept_cd) throws SQLException{
		insertConcept.setString(1, path);
		insertConcept.setString(2, concept_cd);
		insertConcept.setString(3, name);
		insertConcept.setTimestamp(4, sourceTimestamp);
		insertConcept.setString(5, sourceId);
		insertConcept.executeUpdate();
		insertConceptCount ++;
	}
	private String generateMetadataXML(ValueRestriction vr) throws XMLStreamException, OntologyException{
		StringWriter xmlbuf = new StringWriter();
		XMLOutputFactory xmlout = XMLOutputFactory.newInstance();
		XMLStreamWriter xml = xmlout.createXMLStreamWriter(xmlbuf);
		
		//xml.writeStartDocument();
		xml.writeStartElement("ValueMetadata");
		
		xml.writeStartElement("Version");
		xml.writeCharacters("3.02");
		xml.writeEndElement();
		
		xml.writeStartElement("CreationDateTime");
		xml.writeCharacters("10/07/2002 15:08:07"); // TODO use correct time
		xml.writeEndElement();
		
		// TestID, TestName,
		Object[] ev = vr.getEnumerationValues();
		if( ev != null ){
			xml.writeStartElement("DataType");
			xml.writeCharacters("Enum");
			xml.writeEndElement();
			
			xml.writeStartElement("Oktousevalues");
			xml.writeCharacters("Y");
			xml.writeEndElement();
			
			xml.writeStartElement("EnumValues");
			// load enum values
			String[] labels = vr.getEnumerationLabels(locale);
			if( labels == null ){
				// no labels for the specified language
				// use values as labels
				labels = new String[ev.length];
				for( int i=0; i<labels.length; i++ ){
					labels[i] = ev[i].toString();
				}
			}
			for( int i=0; i<ev.length; i++ ){
				xml.writeStartElement("Val");
				xml.writeAttribute("description",labels[i]);
				xml.writeCharacters(ev[i].toString());
				xml.writeEndElement();
			}
			xml.writeEndElement();
		}else{

			String i2b2type;
			QName type = vr.getType();
			if( type != null ){
				switch( type.getLocalPart() ){
				case "integer":
				case "int":
					i2b2type = "Integer";
					break;
				case "float":
				case "decimal":
					i2b2type = "Float";
					break;
				case "string":
				default:
					 i2b2type = "String";
				}
			}else{
				// no type information
				i2b2type = "String";
			}
			xml.writeStartElement("DataType");
			xml.writeCharacters(i2b2type);
			xml.writeEndElement();
			// TODO other datatypes
			// PosFloat, Float, PosInteger, Integer
			// String, largestring
			xml.writeStartElement("Oktousevalues");
			xml.writeCharacters("Y");
			xml.writeEndElement();
			
		}
		xml.writeEndDocument();
		xml.close();
		try {
			xmlbuf.close();
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
		return xmlbuf.toString();
	}
	private void insertMeta(int level, String path_prefix, Concept concept, boolean accessRoot) throws SQLException, OntologyException{
		insertMeta.setInt(1, level);
		String label = concept.getPrefLabel(locale);
		String path_part = label; // TODO unique key sufficient, e.g. try label.hashCode()
		
		if( label == null ){
			// no label for language, try to get neutral label
			label = concept.getPrefLabel(null);
			path_part = label;
			if( label == null ){
				// concept does not have a label
				label = concept.toString();
				path_part = label;//Integer.toHexString(concept.hashCode());
				log.warning("Missing prefLabel for concept "+concept+" substituted with hashCode:"+label);
			}
		}
		// TODO find better way to generate path_part
		path_part = Integer.toHexString(label.hashCode());
		
		// use hashcode
		String path = path_prefix + path_part+"\\";
		insertMeta.setString(2, path);
		insertMeta.setString(3, label);
		// c_synonym_cd
		String synonymCd = "N";
		insertMeta.setString(4, synonymCd); // TODO use set to find out if a concept is used multiple times -> synonym Y
		
		// c_visualattributes and c_basecode
		Concept[] subConcepts = concept.getNarrower();
		String[] conceptIds = concept.getIDs();
		

		if( conceptIds.length == 0 ){
			// no notations ==> concept can not be queried
			// force directory (may be empty if no sub-concepts)
			insertMeta.setString(5, "FA");
			insertMeta.setString(6, null);
		}else if( conceptIds.length == 1 ){
			// exactly one notation
			String visualAttr = (subConcepts.length == 0)?"LA":"FA";
			insertMeta.setString(5, visualAttr);
			insertMeta.setString(6, conceptIds[0]);
		}else if( subConcepts.length == 0 ){
			// no sub-concepts but multiple notations,
			// TODO use MA and 
			insertMeta.setString(5, "LA");
			insertMeta.setString(6, conceptIds[0]);
			// XXX support for multiple conceptIds can be hacked by appending a number to the path for each conceptid and insert each conceptid
			// XXX see some concepts in i2b2 demodata religion with visualattributes M
		}else{
			// has sub concepts and multiple notations,
			// no way to represent this in i2b2
			// just use the first notation and log warning
			insertMeta.setString(5, "FA");
			insertMeta.setString(6, conceptIds[0]);
			log.warning("Ignoring ids other than '"+conceptIds[0]+"' of concept "+concept);
		}

		// c_basecode
		// TODO support multiple ids (e.g. adding virtual leaves)
		if( conceptIds.length != 0 ){			
			// insert into concept_dimension
			insertConceptDimension(path, label, conceptIds[0]);
			// concept has id and can occur in fact table			
			// TODO make sure, each concept_path is inserted only once
		}
		

		
		// c_metadataxml
		ValueRestriction vr = concept.getValueRestriction();
		if( vr == null ){
			insertMeta.setString(7, null);
		}else{
			// build metadata xml
			
			// set value
			try {
				insertMeta.setString(7, generateMetadataXML(vr));
			} catch (XMLStreamException e) {
				// TODO log error
				e.printStackTrace();
				insertMeta.setString(7, null);
			}
		}
		
		// c_dimcode (with concept_dimension.concept_path LIKE)
		insertMeta.setString(8, path);
		
		// c_tooltip
		// try to use concept description
		String descr = concept.getDescription(locale);
		if( descr == null )descr = path; // use path if no description available
		insertMeta.setString(9, path);
		
		// m_applied_path
		insertMeta.setString(10, "@");
		
		// download_date
		insertMeta.setTimestamp(11, sourceTimestamp);
		
		// sourcesystem_cd
		insertMeta.setString(12, sourceId);
		
		insertMeta.executeUpdate();
		insertMetaCount ++;
		
		if( accessRoot ){
			insertAccess.setString(1, sourceId+"_"+Integer.toHexString(concept.hashCode()));
			insertAccess.setInt(2, level);
			insertAccess.setString(3, path);
			insertAccess.setString(4, label);
			insertAccess.setString(5, synonymCd);
			insertAccess.setString(6, "FA");// no leafs on root
			insertAccess.setString(7, path);
			insertAccess.setString(8, descr);
			insertAccess.executeUpdate();
			
			insertAccessCount ++;
		}
		// insert sub concepts
		for( Concept sub : subConcepts ){
			insertMeta(level+1, path, sub, false);
		}
	}
	/*
	public void loadOntology(Class<?> ontologyClass, Map<String,String> config) throws Exception{
		Plugin plugin = Plugin.newInstance(ontologyClass, config);
		assert plugin instanceof Ontology;
		ontology = (Ontology)plugin;
		sourceTimestamp = new Timestamp(ontology.lastModified());
	}*/
	public void setOntology(Ontology ontology){
		this.ontology = ontology;
		this.sourceTimestamp = new Timestamp(ontology.lastModified());
	}

	/**
	 * Each key in src that starts with keyPrefix is copied (without the prefix) and its value to dest
	 * @param src map containing key,value pairs
	 * @param keyPrefix prefix to match src keys
	 * @param dest destination properties
	 */
	private void copyProperties(Map<String,String> src, String keyPrefix, Properties dest){
		src.forEach( 
				(key,value) -> {
					if( key.startsWith(keyPrefix) ){
						dest.put(key.substring(keyPrefix.length()), value);
					}
				} 
		);
	}

	private void openDatabase(Map<String,String> props) throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");
		
		this.config = props;
		sourceId = config.get("meta.sourcesystem_cd");

		String connectString = "jdbc:postgresql://"+props.get("jdbc.host")+":"+props.get("jdbc.port")+"/"+props.get("jdbc.database"); 

		Properties jdbc;
		// use only properties relevant to JDBC
		// meta connection
		jdbc = new Properties();
		copyProperties(config, "meta.jdbc.", jdbc);
		copyProperties(config, "jdbc.", jdbc);
		dbMeta = DriverManager.getConnection(connectString, jdbc);
		dbMeta.setAutoCommit(true);

		// data connection
		jdbc = new Properties();
		copyProperties(config, "data.jdbc.", jdbc);
		copyProperties(config, "jdbc.", jdbc);
		dbData = DriverManager.getConnection(connectString, jdbc);
		dbData.setAutoCommit(true);
		
		prepareStatements();
	}
	
	
	@Override
	public void close()/* throws SQLException, IOException*/{
		// TODO: throw exceptions
		try {
			dbMeta.close();
		} catch (SQLException e) {
			System.out.println("Error closing database connection");
			e.printStackTrace();
		}
		try {
			dbData.close();
		} catch (SQLException e) {
			System.out.println("Error closing database connection");
			e.printStackTrace();
		}
		// don't close ontology, must be closed by caller
		/*
		if( ontology != null )try {
			ontology.close();
		} catch (IOException e) {
			System.err.println("Error closing ontology");
			e.printStackTrace();
		}*/
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String args[])throws FileNotFoundException, IOException{
		if( args.length != 2 ){
			System.err.println("Usage: ontology.properties import.properties");
			System.exit(1);
		}
		File ontConfig = new File(args[0]);
		File impConfig = new File(args[1]);
		if( !ontConfig.canRead() ){
			System.err.println("Unable to read ontology properties from "+args[0]);
			System.exit(1);
		}
		if( !impConfig.canRead() ){
			System.err.println("Unable to read import properties from "+args[1]);
			System.exit(1);
		}

		Properties ont_props = new Properties();
		try( InputStream in = new FileInputStream(ontConfig) ){
			ont_props.load(in);
		}

		String ontClass = ont_props.getProperty("ontology.class");
		if( ontClass == null ){
			System.err.println("Need to specify ontology.class in "+args[0]);
			System.exit(1);
		}

		Properties props;

		// open database
		props = new Properties();
		try( InputStream in = new FileInputStream(impConfig) ){
			props.load(in);
		}
		Import o= null;
		
		try {
			o = new Import((Map)props);
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load database driver");
			e.printStackTrace();
			System.exit(1);
		} catch (SQLException e) {
			System.err.println("Error while accessing database");
			e.printStackTrace();
			System.exit(1);
		}

		
		Class<?> ont;
		try {
			ont = Class.forName(ontClass);
		} catch (ClassNotFoundException e) {
			o.close();
			throw new IllegalArgumentException("Class not found: "+ontClass, e);
		}
		if( !Ontology.class.isAssignableFrom(ont) ){
			o.close();
			throw new IllegalArgumentException(args[0]+" does not implement the Ontology interface");
		}

		Ontology inst=null;
		try {
			inst = (Ontology)Plugin.newInstance(ont, (Map)ont_props);
			o.setOntology(inst);
		} catch (Exception e) {
			System.out.println("Error while loading ontology");
			e.printStackTrace();
			o.close();
			System.exit(1);
		}
		try {
			o.processOntology();
		} catch (SQLException e) {
			System.err.println("Database error");
			e.printStackTrace();
		} catch (OntologyException e) {
			System.err.println("Ontology error");
			e.printStackTrace();
		}
		o.close();
		inst.close();
		
		
	}


}
