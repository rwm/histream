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

/**
 * Accessor which can be used to access and set extension instances for observations.
 * 
 * While extension can also accessed via {@link Observation#getExtension(Class)}, the main
 * difference is that this class does not to lookup the extension type and thus provides
 * faster access. Additionally, this interface can be used to set an extension explicitly
 * via {@link #set(Observation, Object)}.
 * 
 * @author marap1
 *
 * @param <T> extension type
 */
public interface ExtensionAccessor<T> {
	/**
	 * Get the extension type instance. The instance is created automatically on first access.
	 * @param observation observation
	 * @return extension instance
	 */
	T access(Observation observation);
	
	/**
	 * Sets the extension type instance for the given observation.
	 * @param observation observation to set the instance for
	 * @param ext extension instance
	 */
	void set(Observation observation, T ext);
	// TODO: if necessary, create method isAvailable which does not create the instance automatically
	
	/**
	 * Get an extension type instance which is independent of a single observation. 
	 * The instance is identified by the provided arguments.
	 * 
	 * @param args arguments to identify the static instance
	 * @return extension instance
	 * @throws UnsupportedOperationException if this extension does not support static instances
	 * @throws IllegalArgumentException if the arguments are inappropriate to identify instances of this type.
	 */
	T accessStatic(Object... args) throws UnsupportedOperationException, IllegalArgumentException;
}
