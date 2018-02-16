package de.sekmi.histream.etl.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.scripting.AbstractFacts;

@XmlType(name="duplicate-fact")
public class DuplicateFactFilter extends PostProcessingFilter{

	@XmlElement
	public String[] concept;

	@Override
	public void processVisit(AbstractFacts facts) {
		// TODO Auto-generated method stub
		
	}

}
