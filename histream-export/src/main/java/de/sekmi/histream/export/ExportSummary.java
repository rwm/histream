package de.sekmi.histream.export;

public class ExportSummary {
	private int patientCount;
	private int visitCount;

	// TODO maybe add information for additional tables (e.g. table ids and maybe counts)
	
	ExportSummary(int patientCount, int visitCount){
		this.patientCount = patientCount;
		this.visitCount = visitCount;
	}
	public int getPatientCount(){
		return patientCount;
	}
	public int getVisitCount(){
		return visitCount;
	}
}
