package de.sekmi.histream.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MemoryExportWriter implements ExportWriter{
	List<MemoryTable> tables;
	
	public MemoryExportWriter(){
		tables = new LinkedList<>();
	}
	public static class MemoryTable implements TableWriter{
		List<String[]> rows;
		String[] headers;
		String id;
		
		public MemoryTable(String id){
			this.id = id;
			this.rows = new ArrayList<>();
		}
		@Override
		public void header(String[] headers) {
			this.headers = headers;
		}

		@Override
		public void row(String[] columns) throws IOException {
			rows.add(columns);
		}

		@Override
		public void close() throws IOException {
		}
		
		private void dumpRow(String[] row){
			for( int i=0; i<row.length; i++ ){
				if( i != 0 )System.out.print("\t");
				System.out.print(row[i]);
			}
			System.out.println();
		}
		public void dump(){
			System.out.println("Table "+id+":");
			dumpRow(headers);
			for( String[] row : rows ){
				dumpRow(row);
			}
		}
	}
	
	@Override
	public TableWriter openPatientTable() {
		MemoryTable t = new MemoryTable("patients");
		tables.add(t);
		return t;
	}

	@Override
	public TableWriter openVisitTable() {
		MemoryTable t = new MemoryTable("visits");
		tables.add(t);
		return t;
	}

	@Override
	public TableWriter openEAVTable(String id) {
		MemoryTable t = new MemoryTable(id);
		tables.add(t);
		return t;
	}

	public void dump(){
		for( MemoryTable t : tables ){
			t.dump();
			System.out.println();
		}
	}
}
