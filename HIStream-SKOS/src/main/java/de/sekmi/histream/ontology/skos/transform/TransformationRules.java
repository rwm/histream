package de.sekmi.histream.ontology.skos.transform;

public class TransformationRules {
	private Rule[] rules;
	
	public TransformationRules(Rule[] rules){
		this.rules = rules;
	}
	// TODO manage mappings
	// TODO manage generators
	public Rule[] getMapRules(){
		return rules;
	}
	public Rule[] getGeneratorRules(){
		return null;
	}
}
