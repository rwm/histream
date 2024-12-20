package de.sekmi.histream;

import de.sekmi.histream.impl.VisitPatientImpl;

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



/**
 * Single instance which generates all observations. 
 * Manages extensions which enhance/annotate observations.
 * 
 * TODO replace interface with implementation - one implementation is sufficient.
 * 
 * @author marap1
 *
 */
public interface ObservationFactory {
	/**
	 * Register an extension. Registered extensions cannot be removed.
	 * @param extension extension
	 */
	void registerExtension(Extension<?> extension);
	
	<T> Extension<T> getExtension(Class<T> type);

	/**
	 * Get a list of currently registered extensions
	 * @return currently registered extensions
	 */
	//Iterable<Extension<?>> registeredExtensions();
	
	/**
	 * Extensions can be accessed through either via {@link Observation#getExtension(Class)}
	 * or via an {@link ExtensionAccessor}. The latter method is faster, because no hash
	 * lookup of the extensionType needs to be performed.
	 * @param <T> extension type
	 * 
	 * @param extensionType type to get an accessor for
	 * @return extension accessor or null if the extension is not available
	 */
	<T> ExtensionAccessor<T> getExtensionAccessor(Class<T> extensionType);
	
	/**
	 * Create a new observation
	 * @param visit observation's visit
	 * @param conceptId concept code
	 * @param startTime start time for the fact TODO remove start time from constructor but throw exception later if startTime is needed.
	 * @return new observation
	 */
	Observation createObservation(VisitPatientImpl visit, String conceptId, DateTimeAccuracy startTime);
	
	
}
