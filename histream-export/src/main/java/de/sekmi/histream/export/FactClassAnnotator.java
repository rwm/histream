package de.sekmi.histream.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Annotates DOM fact objects with a {@code @class} attribute.
 * 
 * @author R.W.Majeed
 *
 */
class FactClassAnnotator {
	private Map<String, String> conceptMap;
	private List<WildcardRule> wildcardRules;
	
	private class WildcardRule{
		String prefix;
		String classId;
		public WildcardRule(String prefix, String classId){
			this.prefix = prefix;
			this.classId = classId;
		}
	}
	public FactClassAnnotator(){
		this.conceptMap = new HashMap<>();
		this.wildcardRules = new ArrayList<>();
	}

	public void addMapRule(String concept, String classId){
		conceptMap.put(concept, classId);
	}
	public void addWildcardRule(String prefix, String classId){
		wildcardRules.add(new WildcardRule(prefix, classId));
	}
	
	
	public void annotateFact(Node fact) throws IllegalArgumentException{
		if( fact.getNodeType() != Node.ELEMENT_NODE ){
			throw new IllegalArgumentException("Fact node must be of type element");
		}
		if( !fact.getLocalName().equals("fact") ){
			throw new IllegalArgumentException("Local name of node '"+fact.getLocalName()+"' must be 'fact'");
		}
		String concept = ((Element)fact).getAttribute("concept");
		// try to find the concept in the concept map first
		String clazz = conceptMap.get(concept);
		if( clazz == null ){
			// not found, try wildcard rules
			// wildcard rules should be ordered descending by their prefix length,
			// so that the more complex rules are matched first
			for( WildcardRule rule : wildcardRules ){
				if( concept.startsWith(rule.prefix) ){
					// got a match
					clazz = rule.classId;
					break;
				}
			}
		}
		// if found, annotate
		if( clazz != null ){
			((Element)fact).setAttribute("class", clazz);
		}
	}
	
	public void annotateFactSiblings(Node first){
		annotateFact(first);
		Node next = first.getNextSibling();
		while( next != null ){
			if( next.getNodeType() == Node.ELEMENT_NODE && next.getLocalName().equals("fact") ){
				annotateFact(next);
			}
			next = next.getNextSibling();
		}
	}
}
