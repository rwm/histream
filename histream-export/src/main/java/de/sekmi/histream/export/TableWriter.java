package de.sekmi.histream.export;

import java.io.IOException;

public interface TableWriter extends AutoCloseable{
	void header(String[] headers)throws IOException;
	void row(String[] columns) throws IOException;
	@Override
	void close() throws IOException;
}