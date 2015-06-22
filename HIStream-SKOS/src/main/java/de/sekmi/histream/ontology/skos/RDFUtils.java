package de.sekmi.histream.ontology.skos;

import java.util.function.Consumer;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

public class RDFUtils {

	public static Value getObject(RepositoryConnection connection, Resource subject, URI predicate) throws RepositoryException{
		RepositoryResult<Statement> rs = connection.getStatements(subject, predicate, null, false);
		try{ 
			if( rs.hasNext() )return rs.next().getObject();
			else return null;
		}finally{
			rs.close();			
		}
	}
	public static void forEachRDFListItem(RepositoryConnection connection, Resource rdfList, Consumer<Value> consumer) throws RepositoryException{
		do{
			// get value
			Value obj = getObject(connection, rdfList, RDF.FIRST);
			if( obj != null )consumer.accept(obj);
			
			// get rest
			obj = getObject(connection, rdfList, RDF.REST);
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

	public static final Literal getLiteralObject(RepositoryConnection rc, Resource subject, URI predicate) throws RepositoryException {
		Value v = getObject(rc, subject, predicate);
		if( v instanceof Literal ){
			return (Literal)v;
		}else{
			return null;
		}
	}

	public static final String getLocalString(RepositoryConnection rc, Resource subject, URI predicate, String language) throws RepositoryException {
		// empty language same as no language
		if( language != null && language.length() == 0 )
			language = null;
		
		RepositoryResult<Statement> rs = rc.getStatements(subject, predicate, null, false);
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
		return null; // no value found
	}

}
