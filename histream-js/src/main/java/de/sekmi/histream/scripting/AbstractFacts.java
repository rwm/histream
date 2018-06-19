package de.sekmi.histream.scripting;

import java.util.ArrayList;
import java.util.List;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ext.ExternalSourceType;

/**
 * Javascript compatible interface for manipulating a list of
 * facts. Facts can be changed, removed, added.
 * <p>
 * Use {@link #setObservations(List)} to specify the list
 * of observations. The provided list is edited in place.
 * </p>
 * @author R.W.Majeed
 *
 */
public abstract class AbstractFacts {
	protected List<Fact> facts;
	protected List<Observation> sourceList;
	protected ObservationFactory factory;
	protected ExternalSourceType source;

	public AbstractFacts(ObservationFactory factory) {
		this.factory = factory;
		this.facts = new ArrayList<>();
	}
	public void setObservations(List<Observation> observations){
		sourceList = observations;
		facts.clear();
		observations.stream().map(o -> new Fact(o)).forEach(facts::add);
	}
	public void setSource(ExternalSourceType source){
		this.source = source;
	}
	public int size(){return facts.size();}
	
	public List<Fact> facts(){return facts;}
	public int firstIndexOf(String conceptId){
		for( int i=0; i<facts.size(); i++ ){
			if( conceptId.equals(facts.get(i).getConcept()) ){
				return i;
			}
		}
		return -1;
	}
	public Fact remove(String conceptId){
		int i = firstIndexOf(conceptId);
		if( i == -1 ){
			return null;
		}else{
			return removeIndex(i);
		}
	}

	public Fact removeIndex(int index){
		Fact f = facts.remove(index);
		Observation o = sourceList.remove(index);
		// verify that fact and observation are associated
		assert f.getObservation() == o;
		return f;
	}

	public Fact get(int index){
		return facts.get(index);
	}
	public Fact get(String conceptId){
		int i = firstIndexOf(conceptId);
		if( i == -1 ){
			return null;
		}else{
			return facts.get(i);
		}
	}

	protected abstract Observation create(String conceptId);
	
	public Fact add(String conceptId){
		Observation o = create(conceptId);
		o.setSource(source);
		Fact f = new Fact(o);
		sourceList.add(o);
		facts.add(f);
		return f;
	}

	/*
	 * TODO use linked sort to sort both arrays simultaneously by the order given by the comparator
	public void sort(Comparator<Fact> comparator){
		// reorder both arrays
		for( int i=0; i<indices.length; i++ ){
			while( i != indices[i] ){

				// store old target values which will be overridden
				int oldI = indices[indices[i]];
				Fact oldF = facts.get(indices[i]);
				Observation oldO = sourceList.get(indices[i]);

				// replace target values
				facts.set(indices[i], facts.get(i));
				sourceList.set(indices[i], sourceList.get(i));
				indices[indices[i]] = indices[i];

				// move old targets to old values
				indices[i] = oldI;
				facts.set(i, oldF);
				sourceList.set(i, oldO);
			}
		}
	}
*/
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(facts.size() * 128);
		builder.append("[\n");
		for( int i=0; i<facts.size(); i++ ){
			if( i != 0 ) {
				builder.append(",\n");
			}
			builder.append(facts.get(i).toString());
		}
		builder.append("\n]");
		return builder.toString();
	}
}
