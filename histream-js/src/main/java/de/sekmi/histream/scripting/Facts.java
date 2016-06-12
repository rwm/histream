package de.sekmi.histream.scripting;

import java.util.ArrayList;
import java.util.List;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;

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
public class Facts {
	private List<Fact> facts;
	private List<Observation> sourceList;
	private ObservationFactory factory;

	private String patientId;
	private String encounterId;
	private DateTimeAccuracy defaultStartTime;

	public Facts(ObservationFactory factory, String patientId, String encounterId, DateTimeAccuracy defaultStartTime){
		this.factory = factory;
		this.patientId = patientId;
		this.encounterId = encounterId;
		this.defaultStartTime = defaultStartTime;
		this.facts = new ArrayList<>();
	}
	public void setObservations(List<Observation> observations){
		sourceList = observations;
		facts.clear();
		observations.stream().map(o -> new Fact(o)).forEach(facts::add);
	}
	
	public int size(){return facts.size();}
	
	public List<Fact> facts(){return facts;}
	
	
	public Fact add(String conceptId){
		Observation o = factory.createObservation(patientId, conceptId, defaultStartTime);
		o.setEncounterId(encounterId);
		Fact f = new Fact(o);
		sourceList.add(o);
		facts.add(f);
		return f;
	}
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
}
