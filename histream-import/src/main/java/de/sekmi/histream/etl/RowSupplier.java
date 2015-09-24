package de.sekmi.histream.etl;

import java.io.IOException;
import java.time.Instant;
import java.util.function.Supplier;

public abstract class RowSupplier implements Supplier<Object[]>, AutoCloseable{

	public RowSupplier(){
		
	}
	public abstract String[] getHeaders();
	
	@Override
	public abstract Object[] get();

	@Override
	public abstract void close() throws IOException;
	
	/**
	 * Get a the location of the data source and current cursor position.
	 * For a text file, this would be file name and line number. 
	 * A SQL table might return database and table name with row id.
	 * @return location string
	 */
	public abstract String getLocation();
	/**
	 * Get the timestamp for the source. Multiple calls to this method should return the
	 * same timestamp. The source is not allowed to change during reading.
	 * @return timestamp
	 */
	public abstract Instant getTimestamp();
}
