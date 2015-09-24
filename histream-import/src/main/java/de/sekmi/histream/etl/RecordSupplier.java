package de.sekmi.histream.etl;

import java.io.IOException;
import java.util.function.Supplier;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.config.Meta;
import de.sekmi.histream.etl.config.Table;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.ExternalSourceImpl;

public class RecordSupplier<R extends FactRow> implements Supplier<R>, AutoCloseable{
	private RowSupplier rows;
	private Table<R> table;
	private ColumnMap map;
	private ObservationFactory factory;
	private ExternalSourceType source;
	
	public RecordSupplier(RowSupplier rows, Table<R> table, ObservationFactory factory, Meta meta)throws ParseException{
		this.rows = rows;
		this.table = table;
		this.map = table.getColumnMap(rows.getHeaders());
		this.factory = factory;
		this.source = new ExternalSourceImpl(meta.getSourceId(), rows.getTimestamp());
	}
	
	public final ExternalSourceType getSource(){ return this.source;}
	
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
			if( e.getLocation() == null ){
				// add location information
				e.setLocation(rows.getLocation());
			}
			throw new UncheckedParseException(e);
		}
		// fill source information
		for( Observation o : p.getFacts() ){
			o.setSource(source);
		}

		return p;
	}
}
