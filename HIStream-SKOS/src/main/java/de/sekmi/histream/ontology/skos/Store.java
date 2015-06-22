package de.sekmi.histream.ontology.skos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import de.sekmi.histream.Plugin;
import de.sekmi.histream.ontology.Concept;
import de.sekmi.histream.ontology.Ontology;
import de.sekmi.histream.ontology.OntologyException;

public class Store implements Ontology, Plugin {
	private Repository repo;
	private RepositoryConnection rc;
	private final static Concept[] noConcepts = new Concept[]{};
	private long lastModified;
	
	/**
	 * Plugin constructor which accepts configuration parameters.
	 * 
	 * <p>
	 * Configuration keys have the form {@code rdf.baseURI.1}, {@code rdf.file.1}, {@code rdf.format.1}.
	 * rdf.file is mandatory, other parameters are optional.
	 * @param conf configuration parameters
	 * @throws FileNotFoundException if one of the specified files can not be found 
	 * @throws IOException for i/o exceptions while reading the files
	 * @throws RDFParseException if rdf files can not be parsed
	 * @throws RepositoryException for any other exceptions during ontology initialisation
	 */
	public Store(Map<String,String> conf) throws RepositoryException, RDFParseException, IOException{
		int i=1;
		List<File> files = new ArrayList<>();
		List<String> baseURIs = new ArrayList<>();
		// TODO: allow specification of directory
		while( conf.containsKey("rdf.file."+i) ){
			File file = new File(conf.get("rdf.file."+i));
			if( !file.exists() )throw new FileNotFoundException("rdf.file."+i+" not found: "+file.getAbsolutePath());
			files.add(file);
			
			String baseURI = conf.get("rdf.baseURI."+i);
			baseURIs.add(baseURI);
			
			i++;
		}
		initializeRepo(files.toArray(new File[files.size()]), baseURIs.toArray(new String[baseURIs.size()]));
	}
	
	private void initializeRepo(File[] files, String[] baseURIs) throws RepositoryException, RDFParseException, IOException{
	    repo = new SailRepository(new MemoryStore());
	    repo.initialize();

		rc = repo.getConnection();
		
		this.lastModified = 0;

		for( int i=0; i<files.length; i++ ){
			File file = files[i];
			String base_uri = baseURIs[i];
			rc.add(file, base_uri, RDFFormat.TURTLE);
		    
			// use timestamps from files for last modified date
			lastModified = Math.max(lastModified, file.lastModified());
		}		
	}

	public Store(File[] files, String[] baseURIs) throws RepositoryException, RDFParseException, IOException{
		initializeRepo(files, baseURIs);
	}
	
	public Store(File file) throws RepositoryException, RDFParseException, IOException{
		this(new File[]{file}, new String[]{null});
	}
	
	public RepositoryConnection getConnection(){return rc;}

	/**
	 * Collect concepts from a statement iterator to an array.
	 * The iterator will be closed after the method returns
	 * @param result statement iterator which yields SKOSConcept resources as objects
	 * @return array of concepts
	 * @throws RepositoryException for repository errors
	 */
	private Concept[] collectConcepts(RepositoryResult<Statement> result) throws RepositoryException{
		List<Concept> concepts = new ArrayList<>();
		try{
			while( result.hasNext() ){
				Statement s = result.next();
				concepts.add(new ConceptImpl(this, (Resource)s.getObject()));
			}
		}finally{
			result.close();
		}
		if( concepts.size() == 0 )return noConcepts;
		else return concepts.toArray(new Concept[concepts.size()]);
	}
	public Concept[] getTopConcepts()throws OntologyException{
		// TODO use ConceptSchema specified in configuration
		return getRelatedConcepts(null, SKOS.HAS_TOP_CONCEPT);	
	}
	private Concept[] getRelatedConcepts(Resource subject, URI predicate)throws OntologyException{
		RepositoryResult<Statement> rr;
		try {
			rr = rc.getStatements(subject, predicate, null, true);
			return collectConcepts(rr);
		} catch (RepositoryException e) {
			throw new OntologyException(e);
		}		
	}
	Concept[] getBroader(ConceptImpl concept)throws OntologyException{
		return getRelatedConcepts(concept.getResource(), SKOS.BROADER);
	}
	Concept[] getNarrower(ConceptImpl concept)throws OntologyException{
		return getRelatedConcepts(concept.getResource(), SKOS.NARROWER);
	}
	
	public void printTopConcepts() throws OntologyException{
		for( Concept c : getTopConcepts() ){
			System.out.println("Top: "+c);		
		}
	}
	
	private void printSubHierarchy(int indent, Concept concept) throws OntologyException{
		StringBuilder b = new StringBuilder();
		for( int i=0; i<indent; i++ )b.append('\t');
		System.out.println(b.toString()+concept.toString());
		for( Concept c : concept.getNarrower() ){
			printSubHierarchy(indent+1, c);
		}
	}
	public void printConceptHierarchy()throws OntologyException{
		for( Concept c : getTopConcepts() ){
			printSubHierarchy(0, c);
		}
		
	}
	

	@Override
	public Concept getConceptByNotation(String id) throws OntologyException {

		try {
			RepositoryResult<Statement> rs = rc.getStatements(null, SKOS.NOTATION, Literals.createLiteral(rc.getValueFactory(), id), true);
			try{
				if( !rs.hasNext() )return null;
				Resource s = rs.next().getSubject();
				if( rs.hasNext() ){
					// multiple concepts with same id
					// this should not happen
					throw new OntologyException("Notation '"+id+"' with multiple concepts");
				}
				return new ConceptImpl(this, s);
			}finally{
				rs.close();
			}
		}catch( RepositoryException e ){
			throw new OntologyException(e);
		}
	}
	
	String getLocalString(Resource subject, URI predicate, String language) throws OntologyException {
		try {
			return RDFUtils.getLocalString(rc, subject, predicate, language);
		}catch( RepositoryException e ){
			throw new OntologyException(e);
		}
	}

	
	@Override
	public void close() throws IOException {
		try {
			rc.close();
			repo.shutDown();
		} catch (RepositoryException e) {
			throw new IOException(e);
		}
	}

	@Override
	public long lastModified() {
		return lastModified;
	}
	
}

