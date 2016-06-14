package de.sekmi.histream.scripting;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;

/**
 * Facts implementation which doesn't use
 * any observation extensions.
 * 
 * @author R.W.Majeed
 *
 */
public class SimpleFacts extends AbstractFacts {

	// without extensions
	private String patientId;
	private String encounterId;
	private DateTimeAccuracy defaultStartTime;


	public SimpleFacts(ObservationFactory factory, String patientId, String encounterId, DateTimeAccuracy defaultStartTime){
		super(factory);
		this.patientId = patientId;
		this.encounterId = encounterId;
		this.defaultStartTime = defaultStartTime;
	}

	@Override
	protected Observation create(String conceptId) {
		Observation o = factory.createObservation(patientId, conceptId, defaultStartTime);
		if( encounterId != null ){
			o.setEncounterId(encounterId);
		}
		if( source != null ){
			o.setSource(source);
		}
		return o;
	}
}
