package de.sekmi.histream.io.transform;

import java.util.function.Consumer;
import java.util.function.Function;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.Visit;

/**
 * Replace patient and visit information for each observation.
 * 
 * @author Raphael
 *
 */
public class PatientVisitTransformation implements Transformation {

	private Function<Visit, Visit> visitMapper;
	
	
	public PatientVisitTransformation() {
	}
	
	/**
	 * Set the patient mapping functions
	 * 
	 * @param visitMapper map a visit/patient. Assign a different visit to the fact for a
	 * given visit.
	 */
	public void setVisitMapping(Function<Visit,Visit> visitMapper){
		this.visitMapper = visitMapper;
	}
	@Override
	public Observation transform(Observation fact, Consumer<Observation> generatedReceiver)
			throws TransformationException {
		
		if( visitMapper != null ){
			fact.setVisit(visitMapper.apply(fact.getVisit()));	
		}

		return fact;
	}

}
