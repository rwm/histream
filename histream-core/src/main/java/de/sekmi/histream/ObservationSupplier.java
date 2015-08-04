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
	 * Retrieve meta information for this supply of observations.
	 * <p>
	 * Possible keys are source.id, source.timestamp, etl.strategy, order.grouped, order.sorted
	 * <dl>
	 * 	<dt>source.id</dt>
	 * 	<dd>ID for the source which provides the observations
	 * 
	 *  <dt>source.timestamp</dt>
	 *  <dd>Timestamp when the source data was extracted/downloaded/queried</dd>
	 *  
	 *  <dt>etl.strategy</dt>
	 *  <dd>Strategy how to handle imported data. 
	 *  {@code replace-source} will drop any previous imports 
	 *  with the same {@code source.id}, {@code replace-visit} will 
	 *  delete any previous data with the same patient+visit combination.
	 *  </dd>
	 *  
	 *  <dt>order.grouped</dt>
	 *  <dd>If set to true, guarantees that all facts belonging to the same 
	 *  patient+visit combination are provided en bloc.
	 *  </dd>
	 *  
	 *  <dt>order.sorted</dt>
	 *  <dd>If set to true, guarantees that all facts within the same
	 *  patient+visit combination occur in ascending order of start timestamp.
	 *  </dd>
	 * </dl>
	 * @param key meta key
	 * @return value for the meta key
	 */
	String getMeta(String key);
	
	void close() throws Exception;
}
