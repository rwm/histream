package de.sekmi.histream;

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


import java.util.function.Supplier;
import java.util.stream.Stream;

import de.sekmi.histream.impl.ScopedProperty;
import de.sekmi.histream.io.Streams;

/**
 * Supplier of observations.
 * 
 * Also provides meta informaiton via {@link #getMeta(String)}
 * <p>
 * When an instance is constructed, meta information should be read from
 * the file (e.g. etl strategy and other instructions)
 * <p>
 * TODO shouldn't this interface extend Closable?
 * TODO maybe add error handler
 * @author Raphael
 *
 */
public interface ObservationSupplier extends Supplier<Observation>, AutoCloseable{

	/**
	 * Get a stream of observations. Stream will terminate on the first {@code null}
	 * observation.
	 * @return stream
	 */
	public default Stream<Observation> stream(){
		return Streams.nonNullStream(this);
	}
	
	/**
	 * Retrieve meta information for this supply of observations.
	 * For possible keys see {@link #META_ETL_STRATEGY} ...
	 * @param key meta key
	 * @param path 
	 * @return value for the meta key
	 */
	String getMeta(String key, String path);
	
	Iterable<ScopedProperty> getMeta();
	
	void close() throws Exception;
}
