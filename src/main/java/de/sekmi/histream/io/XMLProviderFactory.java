package de.sekmi.histream.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Plugin;

public class XMLProviderFactory implements FileObservationProviderFactory, Plugin{

	public void XMLObservationFactory(Map<String,String> props) {
		// no configuration needed
	}
	@Override
	public void close() throws IOException {
		// don't need to close anything
	}

	@Override
	public FileObservationProvider forFile(File file, ObservationFactory factory) throws IOException {
		try {
			return new XMLObservationProvider(factory, new FileInputStream(file));
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new IOException(e);
		}
	}

}
