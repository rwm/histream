package de.sekmi.histream.etl;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class MemoryTable implements RowSupplier{
	private Row[] rows;
	private String[] headers;
	private Instant timestamp;
	private URL url;
	/** index into rows to retrieve next by {@link #get()} */
	private int pointer;

	public MemoryTable(FileRowSupplier source) {
		headers = source.getHeaders();
		url = source.getSourceURL();
		timestamp = source.getTimestamp();
		// 
		// create array to contain all rows
		ArrayList<Row> list = new ArrayList<>();
		for( Object[] row=source.get(); row!=null; row=source.get()) {
			list.add(new Row(row, source.getLineNo()));
		}
		rows = list.toArray(new Row[list.size()]);
		// point to index 0, get() will retrieve first row
		pointer = 0;
	}

	private class Row{
		Object[] row;
		int lineNo;
		public Row(Object[] row, int lineNo) {
			this.row = row;
			this.lineNo = lineNo;
		}
	}

	@Override
	public String[] getHeaders() {
		return headers;
	}

	private int[] columnPositions(String[] columns) {
		final int[] pos = new int[columns.length];
		// find positions of sort headers
		for( int i=0; i<pos.length; i++ ) {
			int j;
			for( j=0; j<headers.length; j++ ) {
				if( columns[i].equals(headers[j]) ) {
					break;
				}
			}
			if( j == headers.length ) {
				throw new IllegalArgumentException("Sort header '"+columns[j]+"' not found in "+url);
			}
			pos[i] = j;
		}
		return pos;
	}

	/**
	 * Keep only one row per unique occurrence of the specified columns.
	 * Data must be sorted beforehand by the same columns.
	 * @param columns columns which should be unique per row
	 * @throws IllegalArgumentException column header not found
	 */
	public void unique(String[] columns) throws IllegalArgumentException, IllegalStateException{
		final int[] pos = columnPositions(columns);
		unique(pos);
	}
	/**
	 * Keep only one row per unique occurrence of the specified columns.
	 * Data must be sorted beforehand by the same columns.
	 * @param columns columns which should be unique per row
	 * @throws IllegalArgumentException column header not found
	 */
	public void unique(final int[] columns) throws IllegalArgumentException, IllegalStateException{
		if( pointer != 0 ) {
			throw new IllegalStateException("Method may not be used after retrieving rows");
		}
		// make sure we have data
		if( rows.length < 1 ) {
			return;
		}
		boolean[] keep = new boolean[rows.length];
		keep[0] = true; // always keep first row
		int keepCount = 1;

		// determine which rows to keep
		for( int i=1; i<rows.length; i++ ) {
			int j;
			for( j=0; j<columns.length; j++ ) {
				// determine whether to keep row[i]
				// compare to previous row
				Object o1 = rows[i-1].row[columns[j]];
				Object o2 = rows[i].row[columns[j]];
				boolean valueEqual;
				if( o1 == null ) {
					if( o2 == null ) {
						// both null -> same
						valueEqual = true;
					}else {
						// different
						valueEqual = false;
					}
				}else if( o2 == null ) {
					// o1 not null (otherwise if case before) -> different
					valueEqual = false;
				}else {
					valueEqual = o1.equals(o2);
				}
				if( valueEqual == false ) {
					// stop comparing more columns, if one column is found different
					break;
				}
			}
			if( j == columns.length ) {
				// all rows were equal, drop row
				keep[i] = false;				
			}else {
				// at least one column differs relative to previous row
				keep[i] = true;
				keepCount ++;
			}
		}
		// update array, keep only marked
		Row[] keepRows = new Row[keepCount];
		int r = 0;
		for( int i=0; i<rows.length; i++ ) {
			if( keep[i] ) {
				keepRows[r] = rows[i];
				r ++;
			}
		}
		this.rows = keepRows;
	}

		
	/**
	 * Sort the data table by the specified columns
	 * @param columns columns for sort order
	 * @throws IllegalArgumentException column header not found
	 * @throws IllegalStateException rows retrieved before sorting
	 */
	public void sort(String[] columns) throws IllegalArgumentException, IllegalStateException{
		final int[] pos = columnPositions(columns);
		sort(pos);
	}
	/**
	 * Sort the data table by the specified columns
	 * @param columns columns for sort order
	 * @throws IllegalStateException rows retrieved before sorting
	 */
	public void sort(int[] columns) throws IllegalStateException {
		if( pointer != 0 ) {
			throw new IllegalStateException("Method may not be used after retrieving rows");
		}
		Arrays.sort(rows, new Comparator<Row>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(Row r1, Row r2) {
				int order=0;
				for( int i=0; i< columns.length; i++ ) {
					Object o1 = r1.row[columns[i]];
					Object o2 = r2.row[columns[i]];
					// sort nulls first
					if( o1 == null ) {
						order = (o2 == null)?0:-1;
					}else if( o2 == null ) {
						// o1 not null because that would be handled in the first if case
						order = 1;
					}else {
						order = ((Comparable)o1).compareTo(o2);
					}
					// continue with next colum only, if fist column was equal
					if( order != 0 ) {
						break;
					}
				}
				return order;
			}
		});
	}

	

	@Override
	public Object[] get() {
		if( pointer >= rows.length ) {
			return null;
		}
		Object[] row = rows[pointer].row;
		pointer ++;
		return row;
	}

	@Override
	public void close() throws IOException {
		// nothing to do, data lives in memory
	}

	protected int getLineNumber() {
		if( pointer == 0 ) {
			throw new IllegalStateException("Line no requires call to get() first");
		}
		return rows[pointer-1].lineNo;
	}
	@Override
	public String getLocation() {
		return FileRowSupplier.formatLocation(url, getLineNumber());
	}

	@Override
	public Instant getTimestamp() {
		return timestamp;
	}

	public int getRowCount() {
		return rows.length;
	}

	
}
