package de.sekmi.histream.ontology.skos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;




import javax.xml.namespace.QName;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;




import de.sekmi.histream.ontology.EnumValue;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.ValueRestriction;

public class RestrictionImpl implements ValueRestriction {

	QName type;
	ConceptImpl concept;
	Resource resource;
	
	RestrictionImpl(ConceptImpl concept, Resource restriction) throws RepositoryException, OntologyException{
		RepositoryConnection rdf = concept.getStore().getConnection();
		this.concept = concept;
		this.resource = restriction;
		
		if( !rdf.hasStatement(restriction, RDF.TYPE, OWL.RESTRICTION, false) ){
			throw new OntologyException("Type owl:Restriction expected for "+restriction);
		}
		if( !rdf.hasStatement(restriction, OWL.ONPROPERTY, RDF.VALUE, false) ){
			throw new OntologyException("owl:Restriction only supported owl:onProperty rdf:value");
		}
		
		// load type
		Value o = RDFUtils.getObject(rdf, restriction, OWL.ALLVALUESFROM);
		if( o != null ){
			String localPart;
			if( o.equals(XMLSchema.INTEGER) || o.equals(XMLSchema.INT) || o.equals(XMLSchema.LONG) ){
				// integer
				localPart = ((URI)o).getLocalName();
			}else if( o.equals(XMLSchema.POSITIVE_INTEGER) ){
				// TODO positive integer
				localPart = ((URI)o).getLocalName();
			}else if( o.equals(XMLSchema.STRING) ){
				localPart = ((URI)o).getLocalName();
			}else if( o.equals(XMLSchema.DECIMAL) || o.equals(XMLSchema.FLOAT) || o.equals(XMLSchema.DOUBLE) ){
				localPart = ((URI)o).getLocalName();
			}else{
				// TODO: check for extended / restricted types, e.g. positive float
				localPart = null;
			}
			if( localPart != null ){
				this.type = new QName(XMLSchema.NAMESPACE,localPart);
			}
		}
	}
	
	@Override
	public QName getType() {
		return type;
	}

	@Override
	public EnumValue[] getEnumeration(Locale locale) throws OntologyException {
		// load enum
		RepositoryConnection rdf = concept.getStore().getConnection();
		Value o;
		try {
			o = RDFUtils.getObject(rdf, resource, OWL.ONEOF);
			if( o == null )return null;
			
			final String language = (locale==null)?null:locale.toString();
			final List<EnumValue> list = new ArrayList<>();
			RDFUtils.forEachRDFListItem(rdf, (Resource)o, v -> { 
				try {
					String label = RDFUtils.getLocalString(rdf, (Resource)v, SKOS.PREF_LABEL, language);
					Literal literal = RDFUtils.getLiteralObject(rdf, (Resource)v, RDF.VALUE);
					// TODO: use correct types from literal
					list.add(new EnumValue(label, literal.stringValue()));
				} catch (Exception e) {
					// TODO log warning
				} 
			});
			return list.toArray(new EnumValue[list.size()]);
		} catch (RepositoryException e) {
			throw new OntologyException(e);
		}
	}

	@Override
	public Number minInclusive() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number maxInclusive() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer minLength() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer maxLength() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pattern getPattern() {
		// TODO Auto-generated method stub
		return null;
	}


}
