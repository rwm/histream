package de.sekmi.histream;

import java.io.IOException;
import java.io.InputStream;

public interface ObservationParser extends ObservationProvider {

	public void parse(InputStream input) throws IOException;
}
