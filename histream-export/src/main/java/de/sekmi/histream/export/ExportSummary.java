package de.sekmi.histream.export;

public class ExportSummary {
	private int patientCount;
	private int visitCount;
	private int obsCount;

	// TODO maybe add information for additional tables (e.g. table ids and maybe counts)
	
	ExportSummary(int patientCount, int visitCount, int observationCount){
		this.patientCount = patientCount;
		this.visitCount = visitCount;
		this.obsCount = observationCount;
	}
	public int getPatientCount(){
		return patientCount;
	}
	public int getVisitCount(){
		return visitCount;
	}
	public int getObservationCount(){
		return obsCount;
	}
}
