package de.sekmi.histream.export;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Export writer, which will cause IOExceptions at
 * the specified table and operation.
 * 
 * @author R.W.Majeed
 *
 */
public class ExceptionCausingWriter implements ExportWriter{
	private Supplier<IOException> exceptions;
	private WhenToThrow when;
	private WhereToThrow where;

	protected enum WhenToThrow{
		OpenTable, WriteHeader, WriteRow, CloseTable
	}
	protected enum WhereToThrow{
		PatientTable, VisitTable, EAVTable, CloseWriter
	}

	public ExceptionCausingWriter(WhereToThrow where, WhenToThrow when) {
		this.exceptions = () -> new IOException("Expected IO exception");
		this.where = where;
		this.when = when;
	}

	private void throwIf(WhereToThrow whereToThrow, WhenToThrow whenToThrow)throws IOException{
		if( where == whereToThrow && when == whenToThrow ){
			throw exceptions.get();
		}
	}
	private class ExceptionThrowingTable implements TableWriter{
		private WhereToThrow table;
		public ExceptionThrowingTable(WhereToThrow table) {
			this.table = table;
		}
		@Override
		public void header(String[] headers) throws IOException {
			throwIf(table, WhenToThrow.WriteHeader);
		}

		@Override
		public void row(String[] columns) throws IOException {
			throwIf(table, WhenToThrow.WriteRow);
		}

		@Override
		public void close() throws IOException {
			throwIf(table, WhenToThrow.CloseTable);
		}
	}
	@Override
	public TableWriter openPatientTable() throws IOException {
		throwIf(WhereToThrow.PatientTable,WhenToThrow.OpenTable);
		return new ExceptionThrowingTable(WhereToThrow.PatientTable);
	}

	@Override
	public TableWriter openVisitTable() throws IOException {
		throwIf(WhereToThrow.VisitTable,WhenToThrow.OpenTable);
		return new ExceptionThrowingTable(WhereToThrow.VisitTable);
	}

	@Override
	public TableWriter openEAVTable(String id) throws IOException {
		throwIf(WhereToThrow.EAVTable,WhenToThrow.OpenTable);
		return new ExceptionThrowingTable(WhereToThrow.EAVTable);
	}

	@Override
	public void close() throws IOException {
		if( where == WhereToThrow.CloseWriter ){
			throw exceptions.get();
		}
	}

}
