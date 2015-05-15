package de.sekmi.histream;

import de.sekmi.histream.impl.AbstractValue;

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
 * Pair of concept and value. 
 * Basic common interface for both {@link Observation} an {@link Modifier}.
 * 
 * @author marap1
 *
 */
public interface ConceptValuePair {
	/**
	 * Get the concept id
	 * @return concept id
	 */
	public String getConceptId();
	
	/**
	 * Get the value. This method shall not return {@code null}.
	 * If a non-existing value is needed, use {@link AbstractValue#NONE}
	 * @return value value
	 */
	public Value getValue();
}
