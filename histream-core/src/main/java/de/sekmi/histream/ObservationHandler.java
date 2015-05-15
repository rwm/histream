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


import java.util.function.Consumer;

/**
 * Consumes observations. This interface can receive 
 * observations in any order.
 * <p>
 * Some observation handlers might want to assume certain 
 * grouping (e.g. patient/encounter grouping, for patient/encounter export)
 * or strict chronological order (e.g. for calculations, inference engines)
 * XXX this will be addressed in a future releases.
 * 
 * @author Raphael
 *
 */
public interface ObservationHandler extends Consumer<Observation>{
	/**
	 * Receive a single observation/fact.
	 * @param observation fact
	 */
	@Override
	void accept(Observation observation);
	
	void setErrorHandler(Consumer<ObservationException> handler);
	
	/**
	 * Set meta information for this observation handler.
	 * @param key meta key
	 * @param value meta value
	 */
	void setMeta(String key, String value);
}
