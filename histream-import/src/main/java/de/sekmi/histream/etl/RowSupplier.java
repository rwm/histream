package de.sekmi.histream.etl;

import java.io.IOException;
import java.time.Instant;
import java.util.function.Supplier;

public interface RowSupplier extends Supplier<Object[]>, AutoCloseable{

	public abstract String[] getHeaders();
	
	@Override
	public abstract Object[] get();

	@Override
	public abstract void close() throws IOException;
	
	/**
	 * Get the location of the data source and current cursor position
	 * which was used to obtain the row returned by a previous call to {@link #get()}.
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
