package de.sekmi.histream.etl;

import java.util.ArrayList;
import java.util.List;

import de.sekmi.histream.impl.ObservationFactoryImpl;

public class PostVisitFactCount extends VisitPostProcessorQueue{
	private List<VisitCount> counts;

	public static class VisitCount{
		String patid;
		String visid;
		int count;
		public VisitCount(String patid, String visid, int count){
			this.patid = patid;
			this.visid = visid;
			this.count = count;
		}
		public String toString(){
			return "Visit(pid="+patid+", vid="+visid+", facts="+count+")";
		}
	}
	
	public PostVisitFactCount() {
		super(new ObservationFactoryImpl());
		counts = new ArrayList<>();
	}
	@Override
	protected void postProcessVisit() {
		if( getVisit() == null ){
			// null visit for data without visit context (only patient)
			counts.add(new VisitCount(getPatient().getId(),null,getVisitFacts().size()));
			return; // we don't need that;
		}else{
			counts.add(new VisitCount(getPatient().getId(),getVisit().getId(),getVisitFacts().size()));
		}
	}
	
	public List<VisitCount> getCounts(){
		return counts;
	}

}
