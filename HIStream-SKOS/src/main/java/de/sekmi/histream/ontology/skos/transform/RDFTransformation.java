package de.sekmi.histream.ontology.skos.transform;

import java.util.HashMap;
import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.io.Transformation;
import de.sekmi.histream.io.TransformationException;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.skos.Store;

public class RDFTransformation implements Transformation {

	private ObservationFactory factory;
	private Store store;
	private String schema;
	private boolean dropFactsWithoutRules;
	private HashMap<String, TransformationRules> cache;
	
	public RDFTransformation(ObservationFactory factory, Store store, String schema, boolean dropFactsWithoutRules){
		this.store = store;
		this.factory = factory;
		this.schema = schema;
		this.dropFactsWithoutRules = dropFactsWithoutRules;
		this.cache = new HashMap<>();
	}

	@Override
	public Observation transform(Observation fact,
			Consumer<Observation> generatedReceiver) throws TransformationException {
		
		// get rules
		String notation = fact.getConceptId();
		TransformationRules rules;
		if( cache.containsKey(notation) ){
			rules = cache.get(notation);
		}else{
			// retrieve from store
			try {
				rules = store.getConceptTransformations(notation, schema);
			} catch (OntologyException e) {
				// put in cache to prevent repeating errors
				cache.put(notation, null);
				throw new TransformationException("Error retrieving transformation rules",e);
			}
			// put in cache
			cache.put(notation, rules);
		}
		
		if( rules == null ){
			return dropFactsWithoutRules?null:fact;
		}
		
		// TODO perform transformation
		// TODO perform generations
		if( factory != null ){}
		
		return fact;
	}
	

}
