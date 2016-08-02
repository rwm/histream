package de.sekmi.histream.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the table data in memory. Mainly useful for
 * tests.
 * <p>
 * The patient and visit tables can be accessed with the
 * constants {@link #PATIENT_TABLE} and {@link #VISIT_TABLE}.
 * </p>
 * @author R.W.Majeed
 *
 */
public class MemoryExportWriter implements ExportWriter{
	public static final String PATIENT_TABLE = "patients";
	public static final String VISIT_TABLE = "visits";
	
	Map<String, MemoryTable> tables;
	
	public MemoryExportWriter(){
		tables = new HashMap<>();
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
		MemoryTable t = new MemoryTable(PATIENT_TABLE);
		tables.put(t.id, t);
		return t;
	}

	@Override
	public TableWriter openVisitTable() {
		MemoryTable t = new MemoryTable(VISIT_TABLE);
		tables.put(t.id, t);
		return t;
	}

	@Override
	public TableWriter openEAVTable(String id) {
		if( tables.containsKey(id) ){
			throw new IllegalArgumentException("Duplicate table id: "+id);
		}
		MemoryTable t = new MemoryTable(id);
		tables.put(t.id, t);
		return t;
	}

	public void dump(){
		for( MemoryTable t : tables.values() ){
			t.dump();
			System.out.println();
		}
	}

	/**
	 * Access table data
	 * @param table table id
	 * @param column column name
	 * @param row row number
	 * @return data
	 * @throws IndexOutOfBoundsException if any of table, column, row does not exist
	 */
	public String get(String table, String column, int row) throws IndexOutOfBoundsException{
		MemoryTable t = tables.get(table);
		if( t == null ){
			throw new IndexOutOfBoundsException("Non-existing table "+table);
		}
		int index = Arrays.asList(t.headers).indexOf(column);
		if( index == -1 ){
			throw new IndexOutOfBoundsException("Table "+table+" does not contain column "+column);
		}
		if( row < 0 || row >= t.rows.size() ){
			throw new IndexOutOfBoundsException("Unable to access row "+row+" in table "+table+" (has only "+t.rows.size()+" rows)");
		}
		return t.rows.get(row)[index];
	}
	public int rowCount(String table) throws IndexOutOfBoundsException{
		MemoryTable t = tables.get(table);
		if( t == null ){
			throw new IndexOutOfBoundsException("Non-existing table "+table);
		}
		return t.rows.size();
	}
	@Override
	public void close(){
	}
}
