package de.sekmi.histream.etl;

import java.io.IOException;
import java.util.function.Supplier;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.config.Table;

public class RecordSupplier<R> implements Supplier<R>, AutoCloseable{
	private RowSupplier rows;
	private Table<R> table;
	private ColumnMap map;
	private ObservationFactory factory;
	
	public RecordSupplier(RowSupplier rows, Table<R> table, ObservationFactory factory)throws ParseException{
		this.rows = rows;
		this.table = table;
		this.map = table.getColumnMap(rows.getHeaders());
		this.factory = factory;
	}
	
	@Override
	public void close() throws IOException {
		rows.close();
	}

	@Override
	public R get() {
		Object[] row = rows.get();

		if( row == null ){
			// no more rows
			return null;
		}
		R p;
		try {
			p = table.fillRecord(map, row, factory);
		} catch (ParseException e) {
			throw new UncheckedParseException(e);
		}

		return p;
	}
}
