package de.sekmi.histream.etl;

import java.io.IOException;
import java.util.function.Supplier;

import de.sekmi.histream.etl.config.PatientTable;

public class PatientStream implements Supplier<PatientRow>, AutoCloseable{
	RowSupplier rows;
	PatientTable table;
	ColumnMap map;
	
	public PatientStream(RowSupplier rows, PatientTable table) throws IOException{
		this.rows = rows;
		this.table = table;
		this.map = table.getColumnMap(rows.getHeaders());
	}
	
	@Override
	public void close() throws IOException {
		rows.close();
	}

	@Override
	public PatientRow get() {
		Object[] row = rows.get();

		if( row == null ){
			// no more rows
			return null;
		}
		PatientRow p;
		try {
			p = table.fillPatient(map, row);
		} catch (ParseException e) {
			throw new UncheckedParseException(e);
		}

		return p;
	}




}
