package de.sekmi.histream.ontology.skos;

import java.util.ArrayList;
import java.util.Locale;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import de.sekmi.histream.ontology.Concept;
import de.sekmi.histream.ontology.OntologyException;

public class ConceptImpl implements Concept {

	private Resource res;
	private Store store;
	
	public ConceptImpl(Store store, Resource concept){
		this.store = store;
		this.res = concept;
	}
	
	@Override
	public Concept[] getNarrower() throws OntologyException {
		return store.getNarrower(this);
	}
	
	public Resource getResource(){
		return res;
	}
	
	public String toString(){
		return res.toString();
	}

	@Override
	public String[] getIDs() throws OntologyException {
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
	
	

}
