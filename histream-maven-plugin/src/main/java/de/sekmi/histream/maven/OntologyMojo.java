package de.sekmi.histream.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.openrdf.repository.RepositoryException;

import de.sekmi.histream.i2b2.ont.SQLGenerator;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.skos.Store;

@Mojo(name="ontology")//, defaultPhase=LifecyclePhase.GENERATE_RESOURCES)
public class OntologyMojo extends AbstractMojo {	
	@Parameter(required=true)
	FileSet source;

	/**
	 * Destination for the generated resources. 
	 * Defaults to {@code ${project.build.directory}/generated-resources/sql/}
	 */
	@Parameter(required=false, defaultValue="${project.build.directory}/generated-resources/sql")
	File destination;
	
	/**
	 * Overwrite existing files in the destination
	 */
	@Parameter
	boolean overwrite;

//	/**
//	 * Add a COMMIT command at the end of the generated SQL
//	 */
//	@Parameter
//	boolean commit;

	@Parameter
	Properties properties;

	@Parameter(defaultValue="${project}", readonly=true, required=true)
	MavenProject project;

	@Parameter
	Map<String, String> prefixes;

	private void logWarning(String message){
		getLog().warn(message);
	}
	private List<File> getSourceFiles(){
		// read source files
		Objects.requireNonNull(source);
		FileSetManager fsm = new FileSetManager();
		String[] files = fsm.getIncludedFiles(source);
		String base = source.getDirectory();
		getLog().info("Using source base directory: "+base);
		List<File> list = new ArrayList<>(files.length);
		for( int i=0; i<files.length; i++ ){
			getLog().info("Source: "+files[i]);
			list.add(new File(base, files[i]));
		}
		if( list.isEmpty() ){
			getLog().warn("No ontology source files to process");
		}
		return list;
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
			this.getLog().warn("Using project artifact id for missing property meta.sourcesystem_cd");
			map.put("meta.sourcesystem_cd", project.getArtifactId());
		}
		// language
		if( !map.containsKey("ont.language") ){
			this.getLog().warn("Using system default language for missing property ont.language: "+Locale.getDefault().getLanguage());
			map.put("ont.language", Locale.getDefault().getLanguage());
		}
		// TODO use defaults for missing properties meta.table, etc
		return map;
	}
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// copy properties to string map
		Map<String,String> map = loadConfiguration();
		// create destination directories if not existing
		try {
			Files.createDirectories(destination.toPath());
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to create destination directory: "+destination, e);
		}
		Path metaSQL = new File(destination, "meta.sql").toPath();
		Path dataSQL = new File(destination, "data.sql").toPath();

		// load ontology
		Store store;
		try {
			store = new Store(getSourceFiles(), null);
		} catch (RepositoryException | IOException e) {
			throw new MojoExecutionException("Unable to load ontology", e);
		}
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
		
		try {
			SQLGenerator gen = new SQLGenerator(metaSQL, dataSQL, map, overwrite);
			gen.setWarningHandler(this::logWarning);
			gen.writeOntologySQL(store);
			gen.close();
			store.close();
		} catch (IOException | SQLException | OntologyException e) {
			throw new MojoExecutionException("Ontology to SQL transformation failed", e);
		}
	}

}
