package de.sekmi.histream.io;

import java.io.File;
import java.io.IOException;

import de.sekmi.histream.ObservationFactory;

public interface FileObservationProviderFactory {
	FileObservationProvider forFile(File file, ObservationFactory factory)throws IOException;
}
