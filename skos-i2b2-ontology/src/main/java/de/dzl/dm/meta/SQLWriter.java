package de.dzl.dm.meta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

import de.sekmi.histream.i2b2.ont.SQLGenerator;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.skos.Store;

public class SQLWriter {
	private Properties properties;
	private Path destination;
	private boolean overwrite;
	private List<File> rdfFiles;
	private Map<String,String> prefixes;

	private void logWarning(String message){
	}
	private List<File> getSourceFiles(){
		return rdfFiles;
	}
	private Map<String,String> loadConfiguration(){
		Map<String,String> map = new HashMap<>();
		if( properties != null ){
			for( String name : properties.stringPropertyNames() ){
				map.put(name, properties.getProperty(name));
			}
		}
		if( !map.containsKey("meta.sourcesystem_cd") ){
			// use artifact id
			throw new RuntimeException("Property meta.sourcesystem_cd missing");
		}
		// language
		if( !map.containsKey("ont.language") ){
			logWarning("Using system default language for missing property ont.language: "+Locale.getDefault().getLanguage());
			map.put("ont.language", Locale.getDefault().getLanguage());
		}
		// TODO use defaults for missing properties meta.table, etc
		return map;
	}
	private void setPrefixes(Store store){
		if( prefixes != null ){
			String[] ns = new String[prefixes.size()];
			String[] pr = new String[ns.length];
			int i=0;
			for( String key : prefixes.keySet() ){
				pr[i] = key;
				ns[i] = prefixes.get(key);
				i++;
			}			
			store.setNamespacePrefixes(ns, pr);
		}		
	}
	public void write() throws IOException{
		// copy properties to string map
		Map<String,String> map = loadConfiguration();
		// create destination directories if not existing
		Files.createDirectories(destination);
		Path metaSQL = destination.resolve("meta.sql");
		Path dataSQL = destination.resolve("data.sql");

		// load ontology
		Store store;
		try {
			store = new Store(getSourceFiles(), null);
			// use prefixes
			setPrefixes(store);
			// allow user defined relations
			ValueFactory vf = ValueFactoryImpl.getInstance();
			// user defined hasPart
			if( map.containsKey("ont.rel.hasPart") ){
				URI part = vf.createURI(map.get("ont.rel.hasPart"));
				URI parti = null;
				if( map.containsKey("ont.rel.isPartOf") ){
					parti = vf.createURI(map.get("ont.rel.isPartOf"));
				}
				store.setPartOfRelations(part, parti);
			}
		} catch (RepositoryException | IOException e) {
			throw new IOException("Unable to load ontology", e);
		}
		
		try {
			SQLGenerator gen = new SQLGenerator(metaSQL, dataSQL, map, overwrite);
			gen.setWarningHandler(this::logWarning);
			gen.writeOntologySQL(store);
			gen.close();
			store.close();
		} catch (IOException | SQLException | OntologyException e) {
			throw new IOException("Ontology to SQL transformation failed", e);
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException{
		if( args.length < 3 ){
			System.err.println("Need argments: properties-file destination-dir rdf-file1 [rdf-file2 ...]");
			System.exit(-1);
		}
		Properties props = new Properties();
		try( InputStream in = new FileInputStream(args[0]) ){
			props.load(in);
		}
		// check for namespace prefix file
		Properties prefixes = null;
		Path prefixFile = Paths.get("ns-prefixes.properties");
		if( Files.isReadable(prefixFile) ){
			prefixes = new Properties();
			try( InputStream in = Files.newInputStream(prefixFile) ){
				prefixes.load(in);
			}
		}
		Path dest = Paths.get(args[1]);
		List<File> rdf = new ArrayList<>();
		for( int i=2; i<args.length; i++ ){
			rdf.add(new File(args[i]));
		}
		SQLWriter w = new SQLWriter();
		w.destination = dest;
		w.properties = props;
		w.rdfFiles = rdf;
		if( prefixes != null ){
			w.prefixes = new HashMap<>();
			w.prefixes.putAll((Map)prefixes);
		}
		w.write();
	}
}
