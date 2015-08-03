package de.sekmi.histream.etl;

import java.io.IOException;
import java.util.function.Supplier;

public abstract class RowSupplier implements Supplier<Object[]>, AutoCloseable{

	public RowSupplier(){
		
	}
	public abstract String[] getHeaders()throws IOException;
	
	@Override
	public abstract Object[] get();

	@Override
	public abstract void close() throws IOException;
}
