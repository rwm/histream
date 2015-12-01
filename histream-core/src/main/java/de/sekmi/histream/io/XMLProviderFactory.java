package de.sekmi.histream.io;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.Plugin;

@Deprecated
public class XMLProviderFactory implements FileObservationSupplierFactory, Plugin{

	public XMLProviderFactory(Map<String,String> props) {
		// no configuration needed
	}
	@Override
	public void close() throws IOException {
		// don't need to close anything
	}

	@Override
	public ObservationSupplier forFile(File file, ObservationFactory factory) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			return new XMLObservationSupplier(factory, in);
		} catch (XMLStreamException | FactoryConfigurationError e) {
			in.close();
			throw new IOException(e);
		}
	}

}
