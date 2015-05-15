package de.sekmi.histream.ext;

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
 * Extension type which can be identified by a unique id
 * e.g. patient, visit, concept, etc.
 * @author Raphael
 *
 */
public interface IdExtensionType{
	String getId();
	
	/**
	 * Sets the id for the extension type instance.
	 * This method should be called only if the id was undefined before.
	 * The id should not be changed thereafter.
	 * @param id id string
	 */
	void setId(String id);
	
	@Override
	boolean equals(Object obj);
	
	@Override
	int hashCode();
}
