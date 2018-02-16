package de.sekmi.histream.etl.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.scripting.AbstractFacts;
import de.sekmi.histream.scripting.Fact;

@XmlType(name="duplicate-fact")
public class DuplicateFactFilter extends PostProcessingFilter{

	@XmlElement
	public String[] concept;

	private static class FactComparator implements Comparator<Fact>{
		@Override
		public int compare(Fact o1, Fact o2) {
			int cmp = o1.getObservation().getStartTime().compareTo(
						o2.getObservation().getStartTime()	);
			if( cmp == 0 ){
				// if times are equal, sort by concept
				cmp = o1.getConcept().compareTo(o2.getConcept());
			}
			return cmp;
		}
	}
	private void removeAllDuplicates(AbstractFacts facts){
		// order by start and concept
		facts.sort( new FactComparator() );

		ArrayList<Integer> duplicates = new ArrayList<>();
		
		// iterate through facts and store duplicate indices
		DateTimeAccuracy start = null;
		String concept = null;
		for( int i=0; i<facts.size(); i++ ){
			Fact fact = facts.get(i);
			if( start != null ){// nothing to do for first fact	
				if( start.equals(fact.getObservation().getStartTime()) ){
					// start time is equal, check if same concept
					if( concept.equals(fact.getConcept()) ){
						// found duplicate
						duplicates.add(i);
					}
				}
			}
			// remember previous concept
			start = fact.getObservation().getStartTime();
			concept = fact.getConcept();
		}
		// remove duplicates last first
		while( !duplicates.isEmpty() ){
			int index = duplicates.remove(duplicates.size()-1);
			facts.removeIndex(index);
		}
	}
	@Override
	public void processVisit(AbstractFacts facts) {
		// create set for O(1) lookup
//		HashSet<String> match = new HashSet<>(concept.length);
//		Collections.addAll(match, concept);
		// TODO implement for limited concepts
		removeAllDuplicates(facts);
	}

}
