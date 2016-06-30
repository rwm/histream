package de.sekmi.histream.export;

public interface ExportWriter {
	TableWriter openPatientTable();
	TableWriter openVisitTable();
	TableWriter openEAVTable(String id);
}
