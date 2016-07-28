package de.sekmi.histream.export;

import java.io.Closeable;
import java.io.IOException;

/**
 * Write table data. Each returned {@link TableWriter} must
 * be closed individually.
 * <p>
 * The {@link #close()} should be called, after the export
 * processing is complete.
 * </p>
 * @author R.W.Majeed
 *
 */
public interface ExportWriter extends Closeable{
	TableWriter openPatientTable() throws IOException;
	TableWriter openVisitTable() throws IOException;
	TableWriter openEAVTable(String id) throws IOException;
	@Override
	void close() throws IOException;
}
