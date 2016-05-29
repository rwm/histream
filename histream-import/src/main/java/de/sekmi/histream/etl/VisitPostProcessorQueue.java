package de.sekmi.histream.etl;

import java.util.ArrayList;
import java.util.List;

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
	private boolean visitProcessed;
	private Observation nextFact;
	
	public VisitPostProcessorQueue() {
		super();
		visitFacts = new ArrayList<>();
		visitProcessed = false;
	}
	
	@Override
	protected void visitFinished(){
		super.visitFinished();
		postProcessVisit();
		visitFacts.clear();
		this.visitProcessed = true;
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
		if( visitProcessed && !visitFacts.isEmpty() ){
			return visitFacts.remove(0);
		}
		// collect facts for next visit
		visitProcessed = false;
		// on first call, we don't have any fact
		if( nextFact == null ){
			// load first fact
			nextFact = super.get();				
		}
		// as long as we have a fact, collect it until visit is finished
		while( nextFact != null && visitProcessed == false ){
			visitFacts.add(nextFact);
			// next fact
			nextFact = super.get();
		}
		
		if( visitProcessed ){
			if( !visitFacts.isEmpty() ){
				// remove first
				return visitFacts.remove(0);
			}else if( nextFact != null ){
				// at least one more fact available for next visit.
				return this.get(); // may be better to loop instead of recurse
			}else{
				// no more facts, we are done
				return null;
			}
		}else{
			// should never be here.. TODO throw exception
			throw new RuntimeException("Should never happen!");
		}
	}

}
