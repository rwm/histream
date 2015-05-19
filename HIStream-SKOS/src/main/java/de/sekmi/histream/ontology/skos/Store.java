package de.sekmi.histream.ontology.skos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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

import de.sekmi.histream.ontology.Concept;
import de.sekmi.histream.ontology.Ontology;
import de.sekmi.histream.ontology.OntologyException;

public class Store implements Ontology {
	private Repository repo;
	private RepositoryConnection rc;
	private final static Concept[] noConcepts = new Concept[]{};
	
	public Store(File[] files) throws RepositoryException, RDFParseException, IOException{
	    repo = new SailRepository(new MemoryStore());
	    repo.initialize();

		rc = repo.getConnection();
		String baseURI = null;
		
		for( File file : files ){
			rc.add(file, baseURI, RDFFormat.TURTLE);
		}
	}
	
	public Store(File file) throws RepositoryException, RDFParseException, IOException{
		this(new File[]{file});
	}
	
	public RepositoryConnection getConnection(){return rc;}

	/**
	 * Collect concepts from a statement iterator to an array.
	 * The iterator will be closed after the method returns
	 * @param result statement iterator which yields SKOSConcept resources as objects
	 * @return array of concepts
	 * @throws RepositoryException 
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
		// empty language same as no language
		if( language.length() == 0 )
			language = null;
		
		try {
			RepositoryResult<Statement> rs = rc.getStatements(subject, predicate, null, true);
			try{
				while( rs.hasNext() ){
					Value v = rs.next().getObject();
					if( !(v instanceof Literal) )continue;
					Literal l = (Literal)v;
					String lit_lang = l.getLanguage();
					if( language == null && lit_lang == null ){
						// neutral literal (without language)
						return l.getLabel();
					}else if( lit_lang != null && language != null && lit_lang.equals(language) ){
						// language matched
						return l.getLabel();
					}
				}
			}finally{
				rs.close();
			}
		}catch( RepositoryException e ){
			throw new OntologyException(e);
		}
		return null; // no value found
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
	
}

