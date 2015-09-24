package de.sekmi.histream.ontology.skos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.model.vocabulary.XMLSchema;
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
import de.sekmi.histream.ontology.skos.transform.ConditionType;
import de.sekmi.histream.ontology.skos.transform.Rule;
import de.sekmi.histream.ontology.skos.transform.TransformationRules;

public class Store implements Ontology, Plugin {
	private static final Logger log = Logger.getLogger(Store.class.getName());
	private Repository repo;
	private RepositoryConnection rc;
	private final static Concept[] noConcepts = new Concept[]{};
	private long lastModified;
	private Resource inferredContext;
	// SKOS scheme to enforce unique notations
	private Resource scheme;
	
	/**
	 * Plugin constructor which accepts configuration parameters.
	 * 
	 * <p>
	 * Configuration keys have the form {@code rdf.baseURI}, {@code rdf.file.1}, {@code rdf.format.1}.
	 * rdf.file is mandatory, other parameters are optional.
	 * <p>
	 * {@code rdf.baseURI} specifies a URI to resolve any relative URIs that are in the data against. See {@link #Store(Iterable, String)}.
	 * <p>
	 * {@code rdf.skosScheme} can specify the skos:ConceptScheme to find top concepts and unique notations
	 *  
	 * @param conf configuration parameters
	 * @throws FileNotFoundException if one of the specified files can not be found 
	 * @throws IOException for i/o exceptions while reading the files
	 * @throws RepositoryException for any other exceptions during ontology initialisation
	 */
	public Store(Map<String,String> conf) throws RepositoryException, IOException{
		int i=1;
		List<File> files = new ArrayList<>();
		String baseURI = conf.get("rdf.baseURI");
		// TODO: allow specification of directory
		while( conf.containsKey("rdf.file."+i) ){
			File file = new File(conf.get("rdf.file."+i));
			if( !file.exists() )throw new FileNotFoundException("rdf.file."+i+" not found: "+file.getAbsolutePath());
			files.add(file);
			
			i++;
		}
		initializeRepo(files, null, baseURI);
		if( conf.containsKey("rdf.skosScheme") ){
			this.scheme = repo.getValueFactory().createURI(conf.get("rdf.skosScheme")); 
		}
	}
	/**
	 * Infer inverse relations. 
	 * The first predicate is searched. For each found statement the inverse predicate is added if it didn't exist previously. 
	 * @param predicate predicate to search (use the less common one)
	 * @param inversePredicate inverse predicate to add
	 * @return number of inferred statements
	 * @throws RepositoryException error
	 */
	protected int inferInverseRelations(URI predicate, URI inversePredicate) throws RepositoryException{
		RepositoryResult<Statement> res = rc.getStatements(null, predicate, null, false);
		int count=0;
		try{
			while( res.hasNext() ){
				Statement stmt = res.next();
				// try to find whether inverse is already existing
				if( stmt.getObject() instanceof Resource 
						&& !rc.hasStatement((Resource)stmt.getObject(), inversePredicate, stmt.getSubject(), false) )
				{
					rc.add((Resource)stmt.getObject(), inversePredicate, stmt.getSubject(), inferredContext);
					count ++;
				}
			}
		}finally{
			res.close();
		}
		return count;
	}
	private void initializeRepo(Iterable<File> files, Iterable<URL> urls, String baseURI) throws RepositoryException, IOException{
	    repo = new SailRepository(new MemoryStore());
	    repo.initialize();
		rc = repo.getConnection();
		inferredContext = repo.getValueFactory().createURI("http://sekmi.de/histream/inferredInverse");
		this.lastModified = 0;

			if( files != null ){
				for( File file : files ){
					try{
						rc.add(file, baseURI, RDFFormat.TURTLE);
					}catch( RDFParseException e ){
						throw new IOException("Parse error for file "+file+":"+e.getLineNumber()+": "+e.getMessage(),e);
					}
					// use timestamps from files for last modified date
					lastModified = Math.max(lastModified, file.lastModified());
				}
			}
			if( urls != null ){
				for( URL url : urls ){
					try{
						rc.add(url, baseURI, RDFFormat.TURTLE);
					}catch( RDFParseException e ){
						throw new IOException("Parse error for url "+url+":"+e.getLineNumber()+": "+e.getMessage(),e);
					}
					// TODO: determine last modified (should work for file,jar,http urls)
					lastModified = System.currentTimeMillis();
				}
			}
		int inferred = 0;
	    inferred += inferInverseRelations(SKOS.TOP_CONCEPT_OF, SKOS.HAS_TOP_CONCEPT);
	    inferred += inferInverseRelations(SKOS.BROADER, SKOS.NARROWER);
	    inferred += inferInverseRelations(SKOS.NARROWER, SKOS.BROADER);
	    if( inferred != 0 ){
	    	log.fine("Inferred "+inferred+" statements.");
	    }
	    
	}

	public Store(Iterable<File> files, String baseURI) throws RepositoryException, IOException{
		initializeRepo(files,null, baseURI);
	}
	public Store(Iterable<URL> urls) throws RepositoryException, IOException{
		// TODO: clean implementation
		initializeRepo(null, urls, null);
	}
	

	public Store(File file) throws RepositoryException, IOException{
		this(Arrays.asList(new File[]{file}), (String)null);
	}
	public void setConceptScheme(String schemeURI){
		this.scheme = repo.getValueFactory().createURI(schemeURI);
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
		// if this.scheme is null, top concepts of all schemes are returned
		return getRelatedConcepts(this.scheme, SKOS.HAS_TOP_CONCEPT);
	}
	
	@Override
	public Concept[] getTopConcepts(String scheme)throws OntologyException{
		URI schemeURI = null;
		if( scheme != null ){
			schemeURI = repo.getValueFactory().createURI(scheme);
		}
		return getRelatedConcepts(schemeURI, SKOS.HAS_TOP_CONCEPT);
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
	public ConceptImpl getConceptByNotation(String id) throws OntologyException {
		// TODO use getConceptByNotation(id, scheme)
		try {
			RepositoryResult<Statement> rs = rc.getStatements(null, SKOS.NOTATION, Literals.createLiteral(rc.getValueFactory(), id), true);
			try{
				if( !rs.hasNext() )return null;
				Resource s = rs.next().getSubject();
				if( rs.hasNext() ){
					// multiple concepts with same id
					// TODO: check ConceptScheme
					throw new SKOSException(s, "Notation '"+id+"' with multiple concepts");
				}
				return new ConceptImpl(this, s);
			}finally{
				rs.close();
			}
		}catch( RepositoryException e ){
			throw new SKOSException(e);
		}
	}

	/**
	 * Find a concept by notation and concept scheme.
	 * <p>
	 * If {@code null} is specified for the scheme, any concept with the specified notation 
	 * is returned - regardless of scheme.
	 * 
	 * @param notation notation to find
	 * @param conceptScheme scheme for the concept or {@code null}
	 * @return concept
	 * @throws RepositoryException for repository errors
	 */
	public ConceptImpl getConceptByNotation(String notation, URI conceptScheme) throws RepositoryException {
		RepositoryResult<Statement> rs = rc.getStatements(null, SKOS.NOTATION, Literals.createLiteral(rc.getValueFactory(), notation), true);
		Resource concept = null;
		try{
			while( rs.hasNext() ){
				concept = rs.next().getSubject();
				// check scheme
				if( rc.hasStatement(concept, SKOS.IN_SCHEME, conceptScheme, false) ){
					// found!
					break;
				}else{
					// other scheme / no scheme
					concept = null;
				}
			}
		}finally{
			rs.close();
		}
		return new ConceptImpl(this, concept);
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
	
	protected void forEachStatement(Resource subject, URI predicate, Consumer<Statement> consumer) throws RepositoryException{
		RepositoryResult<Statement> rs = rc.getStatements(subject, predicate, null, false);
		try{ 
			while( rs.hasNext() ){
				consumer.accept(rs.next());
			}
		}finally{
			rs.close();			
		}
	}
	protected void forEachObject(Resource subject, URI predicate, Consumer<Value> consumer) throws RepositoryException{
		forEachStatement(subject, predicate, s -> consumer.accept(s.getObject()));
	}
	/**
	 * Retrieve transformation rules for the concept identified by the given notation (restricted to a schema)
	 * TODO move method to ConceptImpl
	 * @param notation notation for the concept
	 * @param schemaURI schema in which to search for the notation. can be null
	 * @return transformation rules or {@code null} if there are no rules
	 * @throws OntologyException for ontology errors
	 */
	public TransformationRules getConceptTransformations(String notation, String schemaURI) throws OntologyException{
		ArrayList<Rule> rules = new ArrayList<Rule>();
		try {
			ConceptImpl c = getConceptByNotation(notation, rc.getValueFactory().createURI(schemaURI));
			if( c == null )return null;
			ArrayList<Value> list = new ArrayList<>();
			forEachObject(c.getResource(), HIStreamOntology.DWH_MAPFACT, list::add);
			for( Value v : list ){
				if( !(v instanceof Resource) ){
					// value should be a resource
					throw new RepositoryException("Not a resource: ("+c.getResource()+", "+HIStreamOntology.DWH_MAPFACT+", [object] <--!! )");
				}
				Rule rule = loadMapFactRule((Resource)v, false);
				rules.add(rule);
			}
		} catch (RepositoryException e) {
			throw new OntologyException(e);
		}
		if( rules.size() == 0 )return null;
		else return new TransformationRules(rules.toArray(new Rule[rules.size()]));
	}
	public void forEachRDFListItem(Resource rdfList, Consumer<Value> consumer) throws RepositoryException{
		do{
			// get value
			Value obj = RDFUtils.getObject(rc, rdfList, RDF.FIRST);
			if( obj != null )consumer.accept(obj);
			
			// get rest
			obj = RDFUtils.getObject(rc, rdfList, RDF.REST);
			if( obj == null ){
				// illegal termination of list
				// TODO throw exception
				rdfList = null;
			}else if( obj.equals(RDF.NIL) ){
				// end of list reached
				rdfList = null;
			}else if( obj instanceof Resource ){
				// remaining list
				rdfList = (Resource)obj;
			}else{
				// illegal termination of list
				// TODO throw exception
				rdfList = null;
			}
		}while( rdfList != null );
	}
	
	private Rule loadOtherwiseRule(Resource r) throws SKOSException, RepositoryException{
		Value targ = RDFUtils.getObject(rc, r, HIStreamOntology.DWH_TARGET);
		if( targ == null )throw new SKOSException(r, "dwh:otherwise must include a dwh:target");
		return new Rule(null,ConditionType.Otherwise,new ConceptImpl(this, (Resource)targ), null);
	}
	private Rule loadMapFactRule(Resource r, boolean disableChoose) throws OntologyException, RepositoryException{
		final Value[] v = new Value[6];
		forEachStatement(r, null, s -> {
				if( s.getPredicate().equals(HIStreamOntology.DWH_CONDITION) ){
					v[0] = s.getObject();
				}else if( s.getPredicate().equals(HIStreamOntology.DWH_CHOOSE) ){
					v[1] = s.getObject();
				}else if( s.getPredicate().equals(HIStreamOntology.DWH_TARGET) ){
					v[2] = s.getObject();
				}else if( s.getPredicate().equals(HIStreamOntology.DWH_MODIFY) ){
					v[3] = s.getObject();
				}else if( s.getPredicate().equals(RDF.VALUE) ){
					v[4] = s.getObject();
				}else if( s.getPredicate().equals(HIStreamOntology.DWH_OTHERWISE) ){
					v[5] = s.getObject();
				}
				// TODO add exceptions to errors
		});
		if( v[0] != null ){
			// condition specified
			// need target
			if( v[2] == null )throw new SKOSException(r, "dwh:mapFact with dwh:condition must also contain dwh:target");
			// choose not allowed
			if( v[1] != null )throw new SKOSException(r, "dwh:condition and dwh:choose cannot be combined");
			// load condition
			if( !(v[0] instanceof Literal) )throw new SKOSException(r, "dwh:condition needs literal object");
			Literal condition = (Literal)v[0];

			// load otherwise
			Rule otherwise = null;
			if( v[5] != null ){
				otherwise = loadOtherwiseRule((Resource)v[5]);
			}
			return Rule.forCondition(condition, new ConceptImpl(this, (Resource)v[2]), otherwise);

		}else if( v[1] != null ){
			// choose specified
			if( !(v[1] instanceof Resource) )throw new SKOSException(r, "dwh:choose must be a resource");
			// target not allowed
			if( v[2] != null )throw new SKOSException(r, "dwh:choose and dwh:target cannot be combined");
			// condition not allowed (v[0] implied)
			final ArrayList<Rule> list = new ArrayList<>();
			final List<Throwable> errors = new LinkedList<>();
			RDFUtils.forEachRDFListItem(rc, (Resource)v[1], l -> {
				// one level of recursion
				try {
					list.add(loadMapFactRule((Resource)l,true));
				} catch (Exception e) {
					errors.add(e);
				}
			});
			// load otherwise
			Rule otherwise = null;
			if( v[5] != null ){
				otherwise = loadOtherwiseRule((Resource)v[5]);
			}
			if( errors.isEmpty() ){
				return new Rule(list.toArray(new Rule[list.size()]), otherwise);
			}else{
				final Throwable first = errors.get(0);
				SKOSException error = new SKOSException(first);
				for( Throwable e : errors ){
					if( e != first )error.addSuppressed(e);
				}
				throw error;
			}
		}else if( v[4] != null ){
			// rdf:value specified (match single value)
			if( !(v[4] instanceof Literal) )throw new SKOSException(r, "rdf:value needs literal object");
			if( v[2] == null )throw new SKOSException(r, "dwh:mapFact with rdf:value must also contain dwh:target");
			Literal value = (Literal)v[4];
			if( !value.getDatatype().equals(XMLSchema.STRING) )throw new SKOSException(r, "rdf:value for comparison must have datatype xsd:string");
			// no otherwise allowed, use null
			return new Rule(value.stringValue(), ConditionType.StringValueEquals, new ConceptImpl(this, (Resource)v[2]), null);
			//return Rule.forCondition(value, new ConceptImpl(this, (Resource)v[2]));
		}else{
			// unsupported mapping
			throw new SKOSException(r, "dwh:mapFact must contain one of dwh:condition, dwh:choose, dwh:value");
		}
	}
}

