package de.sekmi.histream.etl;

import java.io.IOException;
import java.util.function.Supplier;

import de.sekmi.histream.etl.config.Meta;
import de.sekmi.histream.etl.config.Table;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.ExternalSourceImpl;

public class RecordSupplier<R extends FactRow> implements Supplier<R>, AutoCloseable{
	private RowSupplier rows;
	private Table<R> table;
	private ColumnMap map;
	private ExternalSourceType source;
	
	public RecordSupplier(RowSupplier rows, Table<R> table, Meta meta)throws ParseException{
		this.rows = rows;
		this.table = table;
		try{
			this.map = table.getColumnMap(rows.getHeaders());
		}catch( ParseException e ){
			// annotate with location
			e.setLocation(rows.getLocation());
			throw e;
		}
		this.source = new ExternalSourceImpl(meta.getSourceId(), rows.getTimestamp());
	}
	
	public final ExternalSourceType getSource(){ return this.source;}
	
	@Override
	public void close() throws IOException {
		rows.close();
	}

	@Override
	public R get() {
		R p;
		do{
			Object[] row = rows.get();
	
			if( row == null ){
				// no more rows
				return null;
			}
			
			String location = rows.getLocation();
			try {
				p = table.fillRecord(map, row, source, location);
			} catch (ParseException e) {
				if( e.getLocation() == null ){
					// add location information
					e.setLocation(location);
				}
				throw new UncheckedParseException(e);
			}
			// repeat if fillRecord decides to skip the record
		}while( p == null );
		
		return p;
	}
}
