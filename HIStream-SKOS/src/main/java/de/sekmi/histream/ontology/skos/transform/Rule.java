package de.sekmi.histream.ontology.skos.transform;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;

import de.sekmi.histream.ontology.skos.ConceptImpl;
import de.sekmi.histream.ontology.skos.HIStreamOntology;
import de.sekmi.histream.ontology.skos.SKOSException;

public class Rule {
	protected String condition;
	protected ConditionType conditionType;
	protected Rule[] choose;
	protected ConceptImpl target;
	
	protected Rule otherwise;
	
	public static Rule forCondition(Literal condition, ConceptImpl target, Rule otherwise)throws SKOSException{
		URI datatype = condition.getDatatype();
		if( datatype == null ){
			throw new SKOSException(target.getResource(), "Expression without datatype");
		}
		else if( datatype.equals(HIStreamOntology.DWH_XPATH) ){
			// set xpath
			return new Rule(condition.stringValue(), ConditionType.XPath, target, otherwise);
		}else if( datatype.equals(HIStreamOntology.DWH_ECMASCRIPT) ){
			// use ecmascript
			return new Rule(condition.stringValue(), ConditionType.ECMAScript, target, otherwise);
		}else{
			throw new SKOSException(target.getResource(), "Unsupported expression datatype: "+datatype.stringValue());
		}
	}
	public Rule (String condition, ConditionType type, ConceptImpl target, Rule otherwise){
		this.condition = condition;
		this.conditionType = type;
		this.target = target;
		this.otherwise = otherwise;
	}
	
	public Rule(Rule[] choose, Rule otherwise){
		this.choose = choose;
		this.otherwise = otherwise;
	}
	public ConceptImpl getTarget(){
		return target;
	}
	
	public String toString(){
		return "Condition: "+condition+" ("+conditionType+") ->"+target;
	}
}
