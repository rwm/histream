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
	 * Get the timestamp for the source. Multiple calls to this method should return the
	 * same timestamp. The source is not allowed to change during reading.
	 * @return timestamp
	 */
	public abstract Instant getTimestamp();
}
