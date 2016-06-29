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

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationSupplier;

public interface FileObservationSupplierFactory {
	/**
	 * Get an observation supplier for the given input stream.
	 * <p>
	 * The input stream will be wrapped and will be closed automatically
	 * by the {@link ObservationSupplier#close()} method.
	 * </p>
	 * <p>
	 *  If this method fails via a checked exception, the input stream will
	 *  be closed.
	 * </p>
	 * @param in input stream
	 * @param factory factory
	 * @return observation supplier
	 * @throws IOException error (will automatically close the input stream)
	 */
	ObservationSupplier createSupplier(InputStream in, ObservationFactory factory)throws IOException;

	default ObservationSupplier createSupplier(File file, ObservationFactory factory) throws IOException {
		return createSupplier(new FileInputStream(file), factory);
	}

}
