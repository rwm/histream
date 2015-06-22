package de.sekmi.histream.ontology.skos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
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

	Class<?> type;
	EnumValue[] enumValues;
	
	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public EnumValue[] getEnumeration() {
		return enumValues;
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
	
	static RestrictionImpl loadFromRDF(RepositoryConnection rdf, Resource restrictionObject, Locale locale) throws OntologyException{
		RestrictionImpl ret = new RestrictionImpl();
		// TODO: load restriction
		
		try {
			if( !rdf.hasStatement(restrictionObject, RDF.TYPE, OWL.RESTRICTION, false) ){
				throw new OntologyException("Type owl:Restriction expected for "+restrictionObject);
			}
			if( !rdf.hasStatement(restrictionObject, OWL.ONPROPERTY, RDF.VALUE, false) ){
				throw new OntologyException("owl:Restriction only supported owl:onProperty rdf:value");
			}
			
			// load enum
			Value o = RDFUtils.getObject(rdf, restrictionObject, OWL.ONEOF);
			if( o != null ){
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
				ret.enumValues = list.toArray(new EnumValue[list.size()]);
			}
			
			// load type
			o = RDFUtils.getObject(rdf, restrictionObject, OWL.ALLVALUESFROM);
			if( o != null ){
				if( o.equals(XMLSchema.INTEGER) || o.equals(XMLSchema.INT) || o.equals(XMLSchema.LONG) ){
					ret.type = Integer.class;
				}else if( o.equals(XMLSchema.STRING) ){
					ret.type = String.class;
				}else if( o.equals(XMLSchema.DECIMAL) || o.equals(XMLSchema.FLOAT) || o.equals(XMLSchema.DOUBLE) ){
					ret.type = BigDecimal.class;
				}else{
					// TODO: check for extended / restricted types
				}
			}
		} catch (RepositoryException e) {
			throw new OntologyException(e);
		}
		
		return ret;
	}

}
