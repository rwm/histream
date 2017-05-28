package de.sekmi.histream.ontology.skos;

import java.util.ArrayList;
import java.util.Locale;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import de.sekmi.histream.ontology.Concept;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.ValueRestriction;

public class ConceptImpl implements Concept {

	private Resource res;
	private Store store;
	
	public ConceptImpl(Store store, Resource concept){
		this.store = store;
		this.res = concept;
	}
	
	@Override 
	public boolean equals(Object other){
		if( other.getClass() != ConceptImpl.class )return false;
		return res.equals(((ConceptImpl)other).res);
	}
	@Override
	public int hashCode(){
		return res.hashCode();
	}
	
	@Override
	public Concept[] getNarrower() throws OntologyException {
		return store.getNarrower(this);
	}
	
	public Resource getResource(){
		return res;
	}
	Store getStore(){
		return store;
	}
	
	public String toString(){
		return getID();
	}

	@Override
	public String[] getNotations() throws OntologyException {
		ArrayList<String> ids = new ArrayList<>();
		try{
			RepositoryResult<Statement> s =	store.getConnection().getStatements(res, SKOS.NOTATION, null, true);
			try{
				while( s.hasNext() ){
					Value o = s.next().getObject();
					ids.add(o.stringValue());
				}
			}finally{
				s.close();
			}
		}catch( RepositoryException e ){
			throw new OntologyException(e);
		}
		return ids.toArray(new String[ids.size()]);
	}

	@Override
	public Concept[] getBroader() throws OntologyException {
		return store.getBroader(this);
	}

	@Override
	public String getPrefLabel(Locale locale) throws OntologyException {
		String lang = (locale==null)?null:locale.toString(); 
		return store.getLocalString(getResource(), SKOS.PREF_LABEL, lang);
	}

	@Override
	public String getDescription(Locale locale) throws OntologyException {
		String lang = (locale==null)?null:locale.toString(); 
		return store.getLocalString(getResource(), DC.DESCRIPTION, lang);
	}

	@Override
	public Concept[] getParts(boolean inherited) throws OntologyException {
		return store.getParts(this, inherited);
	}

	@Override
	public ValueRestriction getValueRestriction() throws OntologyException {
		
		try {
			RepositoryResult<Statement> rs = store.getConnection().getStatements(getResource(), HIStreamOntology.DWH_RESTRICTION, null, false);
			try{
				if( !rs.hasNext() ){
					return null; // no restriction
				}
				Value obj = rs.next().getObject();
				
				if( !(obj instanceof Resource) ){
					throw new OntologyException("dwh:restriction expected to be a rdf resource");
				}
				ValueRestriction ret = new RestrictionImpl(this, (Resource)obj);
				
				if( rs.hasNext() ){
					throw new OntologyException("More than one dwh:restriction for "+res);
				}
				return ret;
			}finally{
				rs.close();
			}
		} catch (RepositoryException e) {
			throw new OntologyException(e);
		}
	}

	@Override
	public String[] getSchemes() throws OntologyException {
		ArrayList<String> ids = new ArrayList<>();
		try {
			store.forEachObject(this.res, SKOS.IN_SCHEME, scheme -> ids.add(scheme.stringValue()));
		} catch (RepositoryException e) {
			throw new OntologyException(e);
		}
		return ids.toArray(new String[ids.size()]);
	}

	@Override
	public String getID() {
		URI me = (URI)res;
		String prefix = store.getNamespacePrefix(me.getNamespace());
		if( prefix == null ){
			// backward compatibility. this breaks the Ontology.getConceptById call.
			// TODO: use better namespace prefix instead of hashcode
			prefix = Integer.toHexString(me.getNamespace().hashCode());
		}
		return prefix + ":" +me.getLocalName();
	}
	
	

}
