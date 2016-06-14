package de.sekmi.histream.etl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.sekmi.histream.Observation;

/**
 * Fact queue which executes post processing for each complete visit.
 * <p> 
 * All facts for a single visit are collected, before the
 * post processing is performed. For every patient, a {@code null} visit
 * is also included. See {@link #postProcessVisit()}.
 * </p>
 *
 * @author R.W.Majeed
 *
 */
public abstract class VisitPostProcessorQueue extends FactGroupingQueue {

	private List<Observation> visitFacts;
	private Queue<Observation> processedQueue;
	
	public VisitPostProcessorQueue() {
		super();
		visitFacts = new ArrayList<>();
		processedQueue = new LinkedList<>();
	}
	
	@Override
	protected void visitFinished(){
		super.visitFinished();
		postProcessVisit();
		if( !visitFacts.isEmpty() ){
			processedQueue.addAll(visitFacts);
			visitFacts.clear();		
		}
	}
	/**
	 * Get the facts for the currently post-processed visit. The
	 * list can be modified (e.g. add/remove facts).
	 * 
	 * @return facts for the current visit
	 */
	protected List<Observation> getVisitFacts(){
		return visitFacts;
	}
	
	/**
	 * Post-process a single visit. Use {@link #getVisitFacts()} to
	 * get all facts for the visit. Patient and Visit information can
	 * be obtained via {@link #getPatient()} and {@link #getVisit()}.
	 * <p>
	 *  <strong>WARNING:</strong> for every patient, a {@code null}-Visit
	 *  is processed which may contain facts without any visit information.
	 *  In this case, {@link #getVisit()} will return {@code null}.
	 * </p>
	 */
	protected abstract void postProcessVisit();
	
	@Override
	public Observation get(){
		// collect facts for next visit
		while( processedQueue.isEmpty() ){
			// add to visit queue
			Observation next = super.get(); // super.get() will call visitFinished()
			if( next == null ){
				// no more observations.
				break; // we are done.
			}
			visitFacts.add(next);
		}
		
		if( !processedQueue.isEmpty() ){
			return processedQueue.remove();
		}else{
			return null;
		}
	}

}
