package de.sekmi.histream.ontology.skos.transform;

import java.util.HashMap;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.eval.ECMAEvaluator;
import de.sekmi.histream.eval.Engine;
import de.sekmi.histream.eval.ScriptException;
import de.sekmi.histream.eval.StringValueEqualsEngine;
import de.sekmi.histream.impl.XPathEvaluator;
import de.sekmi.histream.io.Transformation;
import de.sekmi.histream.io.TransformationException;
import de.sekmi.histream.ontology.OntologyException;
import de.sekmi.histream.ontology.skos.ConceptImpl;
import de.sekmi.histream.ontology.skos.Store;

/**
 * Transform observations with rules specified in the RDF
 * ontology.
 * 
 * @author R.W.Majeed
 *
 */
public class RDFTransformation implements Transformation {

	private ObservationFactory factory;
	private Store store;
	private String schema;
	private boolean dropFactsWithoutRules;
	private HashMap<String, TransformationRules> cache;
	private Engine engineXPath;
	private Engine engineES;
	
	/**
	 * Creates a new transformation engine.
	 * @param factory factory used to generate new facts
	 * @param store ontology store
	 * @param schema SKOS schema to use together with notation to locate ontology concepts. 
	 * 	For notation, fact.getConceptId() is used. If set to {@code null}, behaves as 
	 * 	if {@link Store#getConceptByNotation(String, org.openrdf.model.URI)} is called with a null schema. 
	 * 
	 * @param dropFactsWithoutRules if set to true, all facts without transformation rules are silently dropped.
	 * @throws TransformationException errors during {@link XPathEvaluator} initialisation 
	 */
	public RDFTransformation(ObservationFactory factory, Store store, String schema, boolean dropFactsWithoutRules)throws TransformationException{
		this.store = store;
		this.factory = factory;
		this.schema = schema;
		this.dropFactsWithoutRules = dropFactsWithoutRules;
		this.cache = new HashMap<>();
		// TODO initialize evaluation engines for XPath and ECMAScript
		try {
			engineXPath = new XPathEvaluator();
		} catch (JAXBException e) {
			throw new TransformationException("Initialization error for XPath engine",e);
		}
		engineES = new ECMAEvaluator();
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
		try {
			Rule[] map = rules.getMapRules();
			for( Rule rule : map ){
				applyRule(rule, fact);
			}
		} catch (OntologyException e) {
			throw new TransformationException("Rule evaluation failed", e);
		}
		
		// TODO perform generations
		if( factory != null ){}
		
		return fact;
	}
	
	
	private boolean applyRule(Rule rule, Observation fact) throws OntologyException, TransformationException{
		ConceptImpl target;
		if( rule.choose != null ){
			int i = -1;
			for( i=0; i<rule.choose.length; i++ ){
				if( applyRule(rule.choose[i], fact) ){
					// rule matched
					target = rule.choose[i].target;
					break;
				}
			}
			if( i == rule.choose.length && rule.otherwise != null ){
				// no match for choose rules, try otherwise
				target = rule.otherwise.target;
			}else{
				return false;
			}
		}else if( rule.condition != null ){
			Engine ng;
			switch( rule.conditionType ){
			case ECMAScript:
				ng = engineES;
				break;
			case XPath:
				ng = engineXPath;
				break;
			case StringValueEquals:
				ng = StringValueEqualsEngine.ENGINE;
				break;
			default:
				throw new TransformationException("Unsupported condition type: "+rule.conditionType);
			}
			
			boolean matched;
			try {
				matched = ng.test(rule.condition, fact);
			} catch (ScriptException e) {
				throw new TransformationException("Evaluation error: "+rule.condition, e);
			}
			if( matched ){
				// match
				target = rule.target;
			}else if( rule.otherwise != null ){
				// try otherwise
				target = rule.otherwise.target;
			}else{
				// no match
				return false;
			}
			
		}else{
			throw new TransformationException("Rule without 'choose' or 'condition'");
		}
		
		// map to target
		String[] ids = target.getNotations();
		if( ids.length == 0 )throw new TransformationException("No notation found in target concept "+target);
		fact.replaceConcept(ids[0]);
		// TODO: is there a way to specify which notation should be used if there are multiple notations?
		return true;

	}

}
