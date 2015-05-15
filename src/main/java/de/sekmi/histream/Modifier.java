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
 * Sub concept of an {@link Observation}. 
 * 
 * Multiple modifiers are allowed for a single observation,
 * as long as each modifier uses a different concept id / modifier code.
 *  
 * @author marap1
 *
 */
public interface Modifier extends ConceptValuePair{
	@Override 
	String getConceptId();
	
	@Override 
	Value getValue();
	
	/**
	 * Sets the value for this modifier
	 * @param value value
	 */
	void setValue(Value value);
}
