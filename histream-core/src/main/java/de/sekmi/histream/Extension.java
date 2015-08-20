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
 * Extensions allow additional information to be stored and retrieved
 * for observations. 
 * 
 * @author Raphael
 *
 * @param <T> type class
 */
public interface Extension<T>{
	/**
	 * Creates a new instance for the given observation. This is only called
	 * once for each observation, usually when the extension requested for
	 * the observation. The instance is then cached automatically.
	 * 
	 * @param observation observation
	 * @return extension extension
	 */
	T createInstance(Observation observation);
	
	/**
	 * Creates a static instance which is independent of a given observation.
	 * <p>
	 * Some extensions do not support independent instances. In this case, all
	 * calls to this method will result in an {@link UnsupportedOperationException}.
	 * @return new instance 
	 * @throws UnsupportedOperationException if instance creation without {@link Observation} is not possible.
	 * @throws IllegalArgumentException if given arguments are unsuitable to instantiate/identify this type.
	 */
	T createInstance(Object... args) throws UnsupportedOperationException, IllegalArgumentException;
	
	/**
	 * Get class of the instance type. Should be a basic interface like Patient, Visit, Location, Concept, etc.
	 * TODO change return type to array, to register all compatible classes
	 * @return instance type
	 */
	Class<?>[] getInstanceTypes();
	
}
