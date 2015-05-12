package de.sekmi.histream.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Plugin;

public class FlatProviderFactory implements FileObservationProviderFactory, Plugin{

	public FlatProviderFactory(Map<String,String> props) {
		// no configuration needed
	}
	@Override
	public void close() throws IOException {
		// don't need to close anything
	}

	@Override
	public FileObservationProvider forFile(File file, ObservationFactory factory) throws IOException {
		return new FlatObservationProvider(factory, new FileInputStream(file));
	}

}
