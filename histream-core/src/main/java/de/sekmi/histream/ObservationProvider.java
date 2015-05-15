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
 * Provider of observations.
 * 
 * An observation provider usually needs an {@link ObservationFactory} to
 * construct the observation it provides. The factory can be specified via 
 * {@link #setObservationFactory(ObservationFactory)}.
 */
public interface ObservationProvider{
	/**
	 * Set the observation handler which will receive observations
	 * produced by this observation provider.
	 * @param consumer observation handler
	 */
	void setHandler(Consumer<Observation> consumer);
	
	/**
	 * Set the observation factory which this provider will use
	 * to construct observations.
	 * @param factory observation factory
	 */
	void setObservationFactory(ObservationFactory factory);
	
	Class<?>[] getSupportedExtensions();
}
