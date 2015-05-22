package de.sekmi.histream.i2b2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import de.sekmi.histream.Plugin;
import de.sekmi.histream.ontology.Concept;
import de.sekmi.histream.ontology.Ontology;
import de.sekmi.histream.ontology.OntologyException;

public class OntologyImport implements AutoCloseable{
	private static final Logger log = Logger.getLogger(OntologyImport.class.getName());
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
	private Timestamp sourceTimestamp;
	
	public OntologyImport(){
		
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

		deleteAccess.setString(1, sourceId+"%");
		count = deleteAccess.executeUpdate();
		System.out.println("Deleted "+count+" rows from "+getAccessTable());

		deleteOnt.setString(1, sourceId);
		count = deleteOnt.executeUpdate();
		System.out.println("Deleted "+count+" rows from "+getMetaTable());

	}
	
	public void processOntology() throws SQLException, OntologyException{
		deleteFromDatabase();

		// parse language for locale
		if( config.get("ont.language") == null ){
			locale = Locale.getDefault();
		}else{
			locale = Locale.forLanguageTag(config.get("ont.language"));
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
		
		Concept[] concepts = ontology.getTopConcepts();
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
	private void insertMeta(int level, String path_prefix, Concept concept, boolean accessRoot) throws SQLException, OntologyException{
		insertMeta.setInt(1, level);
		String label = concept.getPrefLabel(locale);
		String path_part = label; // TODO unique key sufficient, e.g. try label.hashCode()
		
		if( label == null ){
			// no label for language, try to get neutral label
			label = concept.getPrefLabel(null);
			if( label == null ){
				// concept does not have a label
				path_part = Integer.toHexString(concept.hashCode());
				label = concept.toString();
				log.warning("Missing prefLabel for concept "+concept+" substituted with hashCode:"+label);
			}
		}
		String path = path_prefix + path_part+"\\";
		insertMeta.setString(2, path);
		insertMeta.setString(3, label);
		// c_synonym_cd
		String synonymCd = "N";
		insertMeta.setString(4, synonymCd); // TODO use set to find out if a concept is used multiple times -> synonym Y
		
		// c_visualattributes
		Concept[] subConcepts = concept.getNarrower();
		String visualAttr = (subConcepts.length == 0)?"LA":"FA";
		insertMeta.setString(5, visualAttr);

		// c_basecode
		String[] conceptIds = concept.getIDs();
		// TODO support multiple ids (e.g. adding virtual leaves)
		if( conceptIds.length == 0 ){			
			insertMeta.setNull(6, Types.VARCHAR);
		}else{
			// concept has id and can occur in fact table
			insertMeta.setString(6, conceptIds[0]);
			
			if( conceptIds.length > 1 ){
				log.warning("Ignoring ids other than '"+conceptIds[0]+"' of concept "+concept);
			}
			// insert into concept_dimension
			// TODO make sure, each concept_path is inserted only once
			insertConceptDimension(path, label, conceptIds[0]);
			// XXX support for multiple conceptIds can be hacked by appending a number to the path for each conceptid and insert each conceptid
		}
		
		// c_metadataxml
		insertMeta.setString(7, null);
		
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
			insertAccess.setString(6, visualAttr);
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
	public void loadOntology(Class<?> ontologyClass, Map<String,String> config) throws Exception{
		Plugin plugin = Plugin.newInstance(ontologyClass, config);
		assert plugin instanceof Ontology;
		ontology = (Ontology)plugin;
		sourceTimestamp = new Timestamp(ontology.lastModified());
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
	public void openDatabase(Map<String,String> props) throws ClassNotFoundException, SQLException{
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
		ont_props.load(new FileInputStream(ontConfig));

		String ontClass = ont_props.getProperty("ontology.class");
		if( ontClass == null ){
			System.err.println("Need to specify ontology.class in "+args[0]);
			System.exit(1);
		}

		Properties props;
		OntologyImport o = new OntologyImport();

		// open database
		props = new Properties();
		props.load(new FileInputStream(impConfig));
		try {
			o.openDatabase((Map)props);
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

		try {
			o.loadOntology(ont, (Map)ont_props);
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
		
		
	}


	@Override
	public void close() {
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
		if( ontology != null )try {
			ontology.close();
		} catch (IOException e) {
			System.err.println("Error closing ontology");
			e.printStackTrace();
		}
	}
}
