package de.sekmi.histream.etl.validation;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;

public class StartPlusConcept{
	DateTimeAccuracy start;
	String concept;
	public StartPlusConcept(DateTimeAccuracy start, String concept){
		this.start = start;
		this.concept = concept;
	}
	public StartPlusConcept(Observation t) {
		this(t.getStartTime(), t.getConceptId());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((concept == null) ? 0 : concept.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StartPlusConcept other = (StartPlusConcept) obj;
		if (concept == null) {
			if (other.concept != null)
				return false;
		} else if (!concept.equals(other.concept))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}
}