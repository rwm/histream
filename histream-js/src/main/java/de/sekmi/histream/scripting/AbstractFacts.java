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
 * </>
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
			Fact f = facts.remove(i);
			Observation o = sourceList.remove(i);
			// verify that fact and observation are associated
			assert f.getObservation() == o;
			return f;
		}
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

}