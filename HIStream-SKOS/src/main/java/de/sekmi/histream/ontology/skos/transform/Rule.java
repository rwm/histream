package de.sekmi.histream.ontology.skos.transform;

import org.openrdf.model.Literal;

import de.sekmi.histream.ontology.skos.ConceptImpl;

public class Rule {
	protected String condition;
	protected String conditionType;
	protected Rule[] choose;
	protected ConceptImpl target;
	
	protected Rule otherwise;
	
	public Rule(Literal condition, ConceptImpl target){
		this.condition = condition.stringValue();
		this.conditionType = condition.getDatatype().stringValue();
		this.target = target;
	}
	public Rule(Rule[] choose, Rule otherwise){
		this.choose = choose;
	}
	public ConceptImpl getTarget(){
		return target;
	}
	
	public String toString(){
		return "Condition: "+condition+" ("+conditionType+") ->"+target;
	}
}
