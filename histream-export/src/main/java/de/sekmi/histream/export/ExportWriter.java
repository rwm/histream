package de.sekmi.histream.export;

import java.io.IOException;

public interface ExportWriter {
	TableWriter openPatientTable() throws IOException;
	TableWriter openVisitTable() throws IOException;
	TableWriter openEAVTable(String id) throws IOException;
}
