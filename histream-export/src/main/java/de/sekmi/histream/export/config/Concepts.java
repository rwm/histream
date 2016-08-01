package de.sekmi.histream.export.config;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class Concepts {

	@XmlElement(name="group")
	List<ConceptGroup> groups;
	
	@XmlElement(name="concept")
	List<Concept> concepts;
	
	Iterable<Concept> allConcepts(){
		return new IterableIterable<Concept,ConceptGroup>(groups, group -> group.concepts.iterator(), concepts);
	}
	
	public List<ConceptGroup> getGroups(){
		return groups;
	}
	public List<Concept> ungroupedConcepts(){
		return concepts;
	}
}
