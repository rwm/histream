package de.sekmi.histream.i2b2.ont;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.sekmi.histream.i2b2.ont.MetaEntry.Type;
import de.sekmi.histream.i2b2.ont.MetaEntry.Visibility;
import de.sekmi.histream.ontology.Concept;
import de.sekmi.histream.ontology.Ontology;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.ValueRestriction;

/**
 * Import ontology data into i2b2.
 * <p>
 * Use the methods in the following order: 
 * constructor {@link #Import(Map)}, {@link #setOntology(Ontology)}, {@link #processOntology()}, {@link #close()}
 *
 * @author Raphael
 *
 */
public class Import implements AutoCloseable{
	private static final Logger log = Logger.getLogger(Import.class.getName());
	private Ontology ontology;
	private Connection dbMeta;
	private Connection dbData;
	//private Map<String,String> config;
	private Locale locale;
	
	private PreparedStatement insertMeta;
	private PreparedStatement insertMetaModifier;
	private PreparedStatement insertAccess;
	private PreparedStatement insertConcept;
	private PreparedStatement insertModifier;

	// statistics
	private int insertMetaCount;
	private int insertAccessCount;
	private int insertConceptCount;
	private int insertModifierCount;
	private int deleteMetaCount;
	private int deleteAccessCount;
	private int deleteConceptCount;
	private int deleteModifierCount;

	private String sourceId;
	private String sourceIdDelete;
	private String ontScheme;
	private Timestamp sourceTimestamp;
	// configuration
	private String metaBase;
	private String metaTable;
	private String metaAccess;
	private String dataConceptTable;
	private String dataModifierTable;

	private Consumer<String> warningHandler;

	/**
	 * Connect to the i2b2 database.
	 * The current implementation supports only Postgres databases for i2b2 versions &gt;= 1.7.05(?)
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
	 * @throws ClassNotFoundException if database driver not found
	 * @throws SQLException for SQL errors
	 */
	public Import(Map<String,String> props) throws ClassNotFoundException, SQLException{
		openDatabase(props);
		readConfiguration(props);
		prepareStatements();
		warningHandler = log::warning;
	}

	public Import(Connection dbMeta, Connection dbData, Map<String,String> props) throws SQLException{
		this.dbMeta = dbMeta;
		this.dbData = dbData;
		readConfiguration(props);
		prepareStatements();
		warningHandler = log::warning;
	}

	private String getMetaTable(){return metaTable;}
	private String getAccessTable(){return metaAccess;}
	private String getConceptTable(){return dataConceptTable;}
	private String getModifierTable(){return dataModifierTable;}

	public int getInsertMetaCount(){return this.insertMetaCount;}
	public int getDeleteMetaCount(){return this.deleteMetaCount;}
	public int getInsertConceptCount(){return this.insertConceptCount;}
	public int getDeleteConceptCount(){return this.deleteConceptCount;}
	public int getInsertAccessCount(){return this.insertAccessCount;}
	public int getDeleteAccessCount(){return this.deleteAccessCount;}

	private void showWarning(String warning){
		warningHandler.accept(warning);
	}
	private void prepareStatements() throws SQLException{
		insertMeta = dbMeta.prepareStatement("INSERT INTO "+getMetaTable()+"(c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_basecode,c_metadataxml,c_facttablecolumn,c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,c_tooltip,m_applied_path,update_date,download_date,import_date,sourcesystem_cd)VALUES(?,?,?,?,?,?,?,'concept_cd','concept_dimension','concept_path','T','LIKE',?,?,?,current_timestamp,?,current_timestamp,?)");
		insertMetaModifier = dbMeta.prepareStatement("INSERT INTO "+getMetaTable()+"(c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_basecode,c_metadataxml,c_facttablecolumn,c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,c_tooltip,m_applied_path,update_date,download_date,import_date,sourcesystem_cd)VALUES(?,?,?,?,?,?,?,'modifier_cd','modifier_dimension','modifier_path','T','LIKE',?,?,?,current_timestamp,?,current_timestamp,?)");
		String access_table_name = getMetaTable();
		if( access_table_name.indexOf('.') >= 0 ){
			// name contains tablespace
			// find just the name
			access_table_name = access_table_name.substring(access_table_name.indexOf('.')+1);
		}
		insertAccess = dbMeta.prepareStatement("INSERT INTO "+getAccessTable()+"(c_table_cd,c_table_name,c_protected_access,c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_facttablecolumn,c_dimtablename,c_columnname,c_columndatatype,c_operator,c_dimcode,c_tooltip)VALUES(?,'"+access_table_name+"','N',?,?,?,?,?,'concept_cd','concept_dimension','concept_path','T','LIKE',?,?)");
		insertConcept = dbData.prepareStatement("INSERT INTO "+getConceptTable()+"(concept_path,concept_cd,name_char,update_date,download_date,import_date,sourcesystem_cd)VALUES(?,?,?,current_timestamp,?,current_timestamp,?)");
		insertModifier = dbData.prepareStatement("INSERT INTO "+getModifierTable()+"(modifier_path, modifier_cd, name_char, update_date,download_date,import_date,sourcesystem_cd)VALUES(?,?,?,current_timestamp,?,current_timestamp,?)");
		
	}

	public void setWarningHandler(Consumer<String> warningHandler){
		this.warningHandler = warningHandler;
	}
	private void readConfiguration(Map<String,String> config){
		// i2b2 output configuration
		// parse base path
		this.metaBase = config.get("meta.basepath"); // optional

		this.sourceId = config.get("meta.sourcesystem_cd");
		// set meta.sourcesystem_cd.delete to '' to omit the delete
		this.sourceIdDelete = config.getOrDefault("meta.sourcesystem_cd.delete", sourceId);
		if( this.sourceIdDelete.trim().length() == 0 ){
			this.sourceIdDelete = null;
		}
		// tables
		this.metaTable = config.get("meta.table");
		this.metaAccess = config.get("meta.access");
		
		this.dataConceptTable = config.get("data.concept.table");
		this.dataModifierTable = config.get("data.modifier.table");

		Objects.requireNonNull(metaTable, "Need configuration: meta.table");
		Objects.requireNonNull(metaAccess, "Need configuration: meta.access");
		Objects.requireNonNull(dataConceptTable, "Need configuration: data.concept.table");
		Objects.requireNonNull(dataModifierTable, "Need configuration: data.modifier.table");
		// ontology configuration
		// parse language for locale
		if( config.get("ont.language") == null ){
			locale = Locale.getDefault();
		}else{
			locale = Locale.forLanguageTag(config.get("ont.language"));
		}
		// parse scheme
		if( config.get("ont.scheme") != null ){
			this.ontScheme = config.get("ont.scheme");
		}
	}
	private void deleteFromDatabase() throws SQLException{
		if( sourceIdDelete == null ){
			// nothing to do
			return;
		}
		// meta
		PreparedStatement deleteOnt = dbMeta.prepareStatement("DELETE FROM "+getMetaTable()+" WHERE sourcesystem_cd=?");
		PreparedStatement deleteAccess = dbMeta.prepareStatement("DELETE FROM "+getAccessTable()+" WHERE c_table_cd LIKE ?");
		// data
		PreparedStatement deleteModifiers = dbData.prepareStatement("DELETE FROM "+getModifierTable()+" WHERE sourcesystem_cd=?");
		PreparedStatement deleteConcepts = dbData.prepareStatement("DELETE FROM "+getConceptTable()+" WHERE sourcesystem_cd=?");
		
		deleteConcepts.setString(1, sourceIdDelete);
		this.deleteConceptCount = deleteConcepts.executeUpdate();
//		System.out.println("Deleted "+deleteConceptCount+" rows from "+getConceptTable());
		deleteConcepts.close();
		
		// delete modifiers
		deleteModifiers.setString(1, sourceIdDelete);
		this.deleteModifierCount = deleteModifiers.executeUpdate();
		deleteModifiers.close();

		deleteAccess.setString(1, sourceIdDelete+"%");
		this.deleteAccessCount = deleteAccess.executeUpdate();
//		System.out.println("Deleted "+deleteAccessCount+" rows from "+getAccessTable());
		deleteAccess.close();

		deleteOnt.setString(1, sourceIdDelete);
		this.deleteMetaCount = deleteOnt.executeUpdate();
//		System.out.println("Deleted "+deleteMetaCount+" rows from "+getMetaTable());
		deleteOnt.close();
	}
	
	public void processOntology() throws SQLException, OntologyException{
		deleteFromDatabase();
		
		String base = metaBase;
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
		
		Concept[] concepts = ontology.getTopConcepts(this.ontScheme);


		for( Concept c : concepts ){
			insertMeta(base_level, base, c, true);
		}
		
//		System.out.println("Inserted "+insertConceptCount+" rows to "+getConceptTable());
//		System.out.println("Inserted "+insertAccessCount+" rows to "+getAccessTable());
//		System.out.println("Inserted "+insertMetaCount+" rows to "+getMetaTable());
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
	private void insertModifierDimension(String path, String name, String concept_cd) throws SQLException{
		insertModifier.setString(1, path);
		insertModifier.setString(2, concept_cd);
		insertModifier.setString(3, name);
		insertModifier.setTimestamp(4, sourceTimestamp);
		insertModifier.setString(5, sourceId);
		insertModifier.executeUpdate();
		insertModifierCount ++;
	}
	private String generateMetadataXML(ValueRestriction vr) throws XMLStreamException, OntologyException{
		if( vr == null ){
			return null;
		}
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
	/**
	 * Build a readable representation of a concept path.
	 * E.g. remove base path and namespace prefixes from path names
	 * @param path ontology path
	 * @return readable path
	 */
	public String readableConceptPath(String path){
		if( path.startsWith(metaBase) ){
			path = path.substring(metaBase.length());
		}
		StringBuilder b = new StringBuilder();
		int pos = 0;
		boolean done = false;
		char prefix_char;
		do{
			int sep = path.indexOf('\\', pos);
			if( sep == -1 ){
				done = true;
				sep = path.length();
			}else if( sep == path.length()-1 ){
				// empty backslash at end of path
				done = true;
			}
			// skip the http:// part of url prefixes
			if( path.length()-pos > 7 && path.substring(pos,pos+7).equals("http://") ){
				// prefixes will end in #
				prefix_char = '#';
				pos += 7;
			}else{
				// prefix will end in :
				prefix_char = ':';
			}
			// is there a prefix in the path segment?
			int pfx = path.indexOf(prefix_char, pos);
			if( pfx != -1 && pfx < sep ){
				// prefix found, ignore the prefix
				pos = pfx+1;
			}
			b.append('\\').append(path.substring(pos, sep));
			pos = sep+1;
		}while( !done );
		return b.toString();
	}

	private String buildConceptPath(String parentPath, Concept concept){
		String path_part = concept.getID(); // TODO unique key sufficient, e.g. try label.hashCode()
		return parentPath + path_part + "\\";
	}
	private String buildModifierPath(String parentPath, Concept modifier){
		return buildConceptPath(parentPath, modifier);
	}
	private void insertConceptMetaEntry(MetaEntry m) throws SQLException{
		insertMeta.setInt(1, m.level);
		insertMeta.setString(2, m.path);
		insertMeta.setString(3, m.label);
		insertMeta.setString(4, m.synonym?"Y":"N");
		StringBuilder b = new StringBuilder(3);
		switch( m.type ){
		case Container:
			b.append('C');
			break;
		case Folder:
			b.append('F');
			break;
		case Leaf:
			b.append('L');
			break;
		case Multiple:
			b.append('M');
			break;
		default:
			throw new IllegalStateException();
		}
		switch( m.visibility ){
		case Disabled:
			b.append('I');
			break;
		case ACTIVE:
			b.append('A');
			break;
		case Hidden:
			b.append('H');
			break;
		default:
			throw new IllegalStateException();
		}
		insertMeta.setString(5, b.toString());
		insertMeta.setString(6, m.basecode);
		insertMeta.setString(7, m.xml);
		insertMeta.setString(8, m.dimcode);
		insertMeta.setString(9, m.tooltip);
		// m_applied_path always '@' for concepts
		insertMeta.setString(10, "@");
		
		// download_date
		insertMeta.setTimestamp(11, sourceTimestamp);
		
		// sourcesystem_cd
		insertMeta.setString(12, sourceId);
		
		insertMeta.executeUpdate();
		insertMetaCount ++;
	}
	private void insertMeta(int level, String path_prefix, Concept concept, boolean accessRoot) throws SQLException, OntologyException{
		MetaEntry m = new MetaEntry();
		m.level = level;
		m.label = concept.getPrefLabel(locale);
		
		if( m.label == null ){
			// no label for language, try to get neutral label
			m.label = concept.getPrefLabel(null);
			if( m.label == null ){
				// concept does not have a label
				m.label = concept.getID();
				showWarning("Missing prefLabel for concept "+concept+" substituted with ID");
			}
		}
		
		// use hashcode
		m.path = buildConceptPath(path_prefix, concept);

		// c_synonym_cd
		m.synonym = false;
		 // TODO use set to find out if a concept is used multiple times -> synonym Y
		
		// c_visualattributes and c_basecode
		Concept[] subConcepts = concept.getNarrower();
		String[] conceptIds = concept.getNotations();
		
		m.visibility = Visibility.ACTIVE;
		

		
		// c_metadataxml
			// set value
		try {
			m.xml = generateMetadataXML(concept.getValueRestriction());
		} catch (XMLStreamException e) {
			throw new OntologyException("Failed to generate metadata XML for concept "+concept.getID(), e);
		}
		// c_dimcode (with concept_dimension.concept_path LIKE)
		m.dimcode = m.path;
		
		// c_tooltip
		// try to use concept description
		String descr = concept.getDescription(locale);
		if( descr == null ){
			descr = readableConceptPath(m.path); // use path if no description available
		}
		m.tooltip = descr;
		

		if( conceptIds.length == 0 ){
			// no notations ==> concept can not be queried
			// force directory (may be empty if no sub-concepts)
			m.type = Type.Folder;
			// TODO set visibility to disabled
			m.visibility = Visibility.Disabled;
			m.basecode = null;
		}else if( conceptIds.length == 1 ){
			// exactly one notation
			m.type = (subConcepts.length == 0)?Type.Leaf:Type.Folder;
			m.basecode = conceptIds[0];
			// add to concept dimension
			insertConceptDimension(m.path, m.label, conceptIds[0]);
		}else if( subConcepts.length == 0 ){
			// no sub-concepts but multiple notations,
			// TODO use MA and 
			m.type = Type.Multiple;
			MetaEntry sub = m.clone();
			sub.level = m.level + 1;
			sub.type = Type.Leaf;
			for( int i=0; i<conceptIds.length; i++ ){
				sub.basecode = conceptIds[i];
				sub.label = m.label + "-" + conceptIds[i].hashCode();
				sub.path = m.path + sub.label;
				sub.dimcode = sub.path;
				insertConceptDimension(sub.path, sub.label, conceptIds[i]);
				insertConceptMetaEntry(sub);
			}
		}else{
			// has sub concepts and multiple notations,
			// no way to represent this in i2b2
			// just use the first notation and log warning
			m.type = Type.Folder;
			m.basecode = conceptIds[0];
			insertConceptDimension(m.path, m.label, conceptIds[0]);
			showWarning("Ignoring ids other than '"+conceptIds[0]+"' of parent concept "+concept);
		}

		
		insertConceptMetaEntry(m);
		
		// insert modifiers
		insertModifiers(concept, m.path);

		if( accessRoot ){
			insertAccess.setString(1, sourceId+"_"+Integer.toHexString(concept.hashCode()));
			insertAccess.setInt(2, level);
			insertAccess.setString(3, m.path);
			insertAccess.setString(4, m.label);
			insertAccess.setString(5, "N");
			insertAccess.setString(6, "FA");// no leafs on root
			insertAccess.setString(7, m.path);
			insertAccess.setString(8, m.tooltip);
			insertAccess.executeUpdate();
			
			insertAccessCount ++;
		}
		// insert sub concepts
		for( Concept sub : subConcepts ){
			insertMeta(level+1, m.path, sub, false);
		}
	}

	private void insertModifiers(Concept concept, String concept_path) throws OntologyException, SQLException{
		// TODO write modifier dimension
		Concept[] parts = concept.getParts(false);
		if( parts == null ){
			return; // nothing to do
		}
		for( Concept part : parts ){
			insertModifierMetaTree(concept_path, 1, part, "\\");
		}
	}

	private void insertModifierMetaEntry(MetaEntry entry) throws SQLException{
		insertMetaModifier.setInt(1, entry.level); // c_hlevel
		insertMetaModifier.setString(2, entry.path); // c_fullname
		// c_name
		insertMetaModifier.setString(3, entry.label);
		// c_synonym_cd
		insertMetaModifier.setString(4, entry.synonym?"Y":"N");
		// c_visualattributes
		StringBuilder b = new StringBuilder(3);
		switch( entry.type ){
		case Container:
			b.append('O');
			break;
		case Folder:
			b.append('D');
			break;
		case Leaf:
			b.append('R');
			break;
		default:
			throw new IllegalStateException();
		}
		switch( entry.visibility ){
		case Disabled:
			b.append('I');
			break;
		case ACTIVE:
			b.append('A');
			break;
		case Hidden:
			b.append('H');
			break;
		default:
			throw new IllegalStateException();
		}
		insertMetaModifier.setString(5, b.toString());
		// c_basecode
		insertMetaModifier.setString(6, entry.basecode);
		// c_metadataxml
		insertMetaModifier.setString(7, entry.xml);
		// c_dimcode
		insertMetaModifier.setString(8, entry.dimcode);
		// c_tooltip
		insertMetaModifier.setString(9, entry.tooltip);
		// m_appliedpath
		insertMetaModifier.setString(10, entry.modpath);
		// update, download, import dates
		insertMetaModifier.setTimestamp(11, sourceTimestamp);
		// sourcesystem_cd
		insertMetaModifier.setString(12, sourceId);
		insertMetaModifier.executeUpdate();
		insertMetaCount ++;
	}
	private void insertModifierMetaTree(String concept_path, int level, Concept modifier, String path_prefix) throws OntologyException, SQLException{
		// fill modifier dimension first
		String[] ids = modifier.getNotations();
		String path = buildModifierPath(path_prefix, modifier);
		String label = modifier.getPrefLabel(locale);
		MetaEntry e = new MetaEntry();
		if( ids.length == 0 ){
			// no code
			e.basecode = null;
		}else if( ids.length == 1 ){
			// single code
			e.basecode = ids[0];
			insertModifierDimension(path, label, ids[0]);
		}else{
			// multiple notations
			showWarning("Using first notation - multiple notations not allowed for i2b2 modifiers: "+modifier.getID());
			e.basecode = ids[0];
			insertModifierDimension(path, label, ids[0]);
		}
		// "INSERT INTO i2b2 (c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_basecode,c_metadataxml,c_facttablecolumn,c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,c_tooltip,m_applied_path,update_date,download_date,import_date,sourcesystem_cd)VALUES(?,?,?,?,?,?,?,'concept_cd','concept_dimension','concept_path','T','LIKE',?,?,?,current_timestamp,?,current_timestamp,?)");
		e.level = level;
		e.path = path;
		e.label = label;
		e.synonym = false;

		Concept[] narrower = modifier.getNarrower();
		if( narrower.length == 0 ){
			// leaf
			e.type = Type.Leaf;
		}else{
			e.type = Type.Folder;
		}
		e.visibility = Visibility.ACTIVE;
		try{
			e.xml = generateMetadataXML(modifier.getValueRestriction());
		} catch (XMLStreamException ex) {
			throw new OntologyException("Failed to generate metadata XML for modifier "+modifier.getID(), ex);
		}
		e.dimcode = e.path;
		e.modpath = concept_path+"%";
		e.tooltip =  modifier.getDescription(locale);
		insertModifierMetaEntry(e);
		// recurse into nested modifier structure
		for( Concept nested : narrower ){
			insertModifierMetaTree(concept_path, level+1, nested, path);
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


	private void openDatabase(Map<String,String> props) throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");

		//String connectString = "jdbc:postgresql://"+props.get("jdbc.host")+":"+props.get("jdbc.port")+"/"+props.get("jdbc.database"); 
		// use only properties relevant to JDBC
		// meta connection
		dbMeta = PostgresUtil.getConnection(props,new String[]{"jdbc.","meta.jdbc."});
		dbMeta.setAutoCommit(true);

		// data connection
		dbData = PostgresUtil.getConnection(props,new String[]{"jdbc.","data.jdbc."});
		dbData.setAutoCommit(true);
	}
	
	
	@Override
	public void close()/* throws SQLException, IOException*/{
		// TODO: throw exceptions
		try {
			dbMeta.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Error closing database connection", e);
		}
		try {
			dbData.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Error closing database connection", e);
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

	
//	Import.main(new String[]{"../HIStream-i2b2/examples/skos-ontology.properties","../HIStream-i2b2/examples/i2b2-ont-import.properties"});

//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public static void main(String args[])throws FileNotFoundException, IOException{
//		if( args.length != 2 ){
//			System.err.println("Usage: ontology.properties import.properties");
//			System.exit(1);
//		}
//		File ontConfig = new File(args[0]);
//		File impConfig = new File(args[1]);
//		if( !ontConfig.canRead() ){
//			System.err.println("Unable to read ontology properties from "+args[0]);
//			System.exit(1);
//		}
//		if( !impConfig.canRead() ){
//			System.err.println("Unable to read import properties from "+args[1]);
//			System.exit(1);
//		}
//
//		Properties ont_props = new Properties();
//		try( InputStream in = new FileInputStream(ontConfig) ){
//			ont_props.load(in);
//		}
//
//		String ontClass = ont_props.getProperty("ontology.class");
//		if( ontClass == null ){
//			System.err.println("Need to specify ontology.class in "+args[0]);
//			System.exit(1);
//		}
//
//		Properties props;
//
//		// open database
//		props = new Properties();
//		try( InputStream in = new FileInputStream(impConfig) ){
//			props.load(in);
//		}
//		Import o= null;
//		
//		try {
//			o = new Import((Map)props);
//		} catch (ClassNotFoundException e) {
//			System.err.println("Unable to load database driver");
//			e.printStackTrace();
//			System.exit(1);
//		} catch (SQLException e) {
//			System.err.println("Error while accessing database");
//			e.printStackTrace();
//			System.exit(1);
//		}
//
//		
//		Class<?> ont;
//		try {
//			ont = Class.forName(ontClass);
//		} catch (ClassNotFoundException e) {
//			o.close();
//			throw new IllegalArgumentException("Class not found: "+ontClass, e);
//		}
//		if( !Ontology.class.isAssignableFrom(ont) ){
//			o.close();
//			throw new IllegalArgumentException(args[0]+" does not implement the Ontology interface");
//		}
//
//		Ontology inst=null;
//		try {
//			inst = (Ontology)Plugin.newInstance(ont, (Map)ont_props);
//			o.setOntology(inst);
//		} catch (Exception e) {
//			System.out.println("Error while loading ontology");
//			e.printStackTrace();
//			o.close();
//			System.exit(1);
//		}
//		try {
//			o.processOntology();
//		} catch (SQLException e) {
//			System.err.println("Database error");
//			e.printStackTrace();
//		} catch (OntologyException e) {
//			System.err.println("Ontology error");
//			e.printStackTrace();
//		}
//		o.close();
//		inst.close();
//		
//		
//	}


}
