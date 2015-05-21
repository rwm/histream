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
	private Connection db;
	private Map<String,String> config;
	private Locale locale;
	
	private PreparedStatement insertOnt;
	private PreparedStatement deleteOntSource;
	
	private String sourceId;
	private long sourceTimestamp;
	
	public OntologyImport(){
		
	}
	
	private String getOntTable(){return config.get("ont.i2b2.table");}
	
	private void prepareStatements() throws SQLException{
		insertOnt = db.prepareStatement("INSERT INTO "+getOntTable()+"(c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_basecode,c_metadataxml,c_facttablecolumn,c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,c_tooltip,m_applied_path,update_date,download_date,import_date,sourcesystem_cd)VALUES(?,?,?,?,?,?,?,'concept_cd','concept_dimension','concept_path','T','LIKE',?,?,?,current_timestamp,?,current_timestamp,?)");
		deleteOntSource = db.prepareStatement("DELETE FROM "+getOntTable()+" WHERE sourcesystem_cd=?");
	}
	public int deleteFromSource(String sourceId) throws SQLException{
		deleteOntSource.setString(1, sourceId);
		return deleteOntSource.executeUpdate();
	}
	
	public void processOntology() throws SQLException, OntologyException{
		sourceId = config.get("ont.i2b2.sourcesystem_cd");
		sourceTimestamp = ontology.lastModified();
		deleteFromSource(sourceId);

		// parse language for locale
		if( config.get("ont.language") == null ){
			locale = Locale.getDefault();
		}else{
			locale = Locale.forLanguageTag(config.get("ont.language"));
		}
		
		// parse base path
		String base = config.get("ont.i2b2.basepath");
		int base_level;
		if( base != null && !base.equals("\\") ){
			String[] baseParts = base.split("\\\\");
			if( baseParts.length < 2 || baseParts[0].length() != 0 || !base.endsWith("\\") )
				throw new IllegalArgumentException("ont.i2b2.basepath must start and end with a backslash (\\)");
			base_level = baseParts.length - 1;
		}else{
			base = "\\";
			base_level = 0;
		}
		
		Concept[] concepts = ontology.getTopConcepts();
		for( Concept c : concepts ){
			insertConcept(base_level, base, c);
		}
	}
	private void insertConcept(int level, String path_prefix, Concept concept) throws SQLException, OntologyException{		
		insertOnt.setInt(1, level);
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
		insertOnt.setString(2, path);
		insertOnt.setString(3, label);
		// c_synonym_cd
		insertOnt.setString(4, "N"); // TODO use set to find out if a concept is used multiple times -> synonym Y
		
		// c_visualattributes
		Concept[] subConcepts = concept.getNarrower();
		insertOnt.setString(5, (subConcepts.length == 0)?"LA":"FA");

		// c_basecode
		String[] conceptIds = concept.getIDs();
		// TODO support multiple ids (e.g. adding virtual leaves)
		if( conceptIds.length == 0 ){			
			insertOnt.setNull(6, Types.VARCHAR);
		}else{
			// concept has id and can occur in fact table
			insertOnt.setString(6, conceptIds[0]);
			
			if( conceptIds.length > 1 ){
				log.warning("Ignoring ids other than '"+conceptIds[0]+"' of concept "+concept);
			}
			// TODO insert into concept_dimension
		}
		
		// c_metadataxml
		insertOnt.setString(7, null);
		
		// c_dimcode (with concept_dimension.concept_path LIKE)
		insertOnt.setString(8, path);
		
		// c_tooltip
		// try to use concept description
		String descr = concept.getDescription(locale);
		if( descr == null )descr = path; // use path if no description available
		insertOnt.setString(9, path);
		
		// m_applied_path
		insertOnt.setString(10, "@");
		
		// download_date
		insertOnt.setTimestamp(11, new Timestamp(sourceTimestamp));
		
		// sourcesystem_cd
		insertOnt.setString(12, sourceId);
		
		insertOnt.executeUpdate();
		
		// insert sub concepts
		for( Concept sub : subConcepts ){
			insertConcept(level+1, path, sub);
		}
	}
	public void loadOntology(Class<?> ontologyClass, Map<String,String> config) throws Exception{
		Plugin plugin = Plugin.newInstance(ontologyClass, config);
		assert plugin instanceof Ontology;
		ontology = (Ontology)plugin;
	}

	public void openDatabase(Map<String,String> props) throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");
		this.config = props;
		Properties jdbcProps = new Properties();
		// TODO put only properties relevant to jdbc
		jdbcProps.putAll(props);
		db = DriverManager.getConnection("jdbc:postgresql://"+props.get("host")+":"+props.get("port")+"/"+props.get("database"), jdbcProps);
		db.setAutoCommit(true);
		prepareStatements();
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String args[])throws FileNotFoundException, IOException{
		if( args.length != 3 ){
			System.err.println("Usage: class.path.for.Ontology ontology.properties database.properties");
			System.exit(1);
		}
		Class<?> ont;
		try {
			ont = Class.forName(args[0]);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Class not found: "+args[0], e);
		}
		if( !Ontology.class.isAssignableFrom(ont) ){
			throw new IllegalArgumentException(args[0]+" does not implement the Ontology interface");
		}
		File ontConfig = new File(args[1]);
		File impConfig = new File(args[2]);
		if( !ontConfig.canRead() ){
			System.err.println("Unable to read ontology properties from "+args[1]);
			System.exit(1);
		}
		if( !impConfig.canRead() ){
			System.err.println("Unable to read import properties from "+args[2]);
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

		
		props = new Properties();
		props.load(new FileInputStream(ontConfig));

		try {
			o.loadOntology(ont, (Map)props);
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
			db.close();
		} catch (SQLException e) {
			System.out.println("Error closing database");
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
