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
 * Interface for patient store. Can be used to manage patients (e.g. merge)
 * TODO add classes for measuring performance for random access of patient store / visit store / concept store
 * @author Raphael
 *
 */
public interface PatientStore {
	Patient retrieve(String id);
	void merge(Patient patient, String additionalId, ExternalSourceType source);
	
	/**
	 * Get alias ids for the given patient (e.g. resulting from a merge) 
	 * @param patient
	 * @return
	 */
	String[] getAliasIds(Patient patient);
	
	/**
	 * Deletes the patient identified by given id. This method does not remove any other associated
	 * data e.g. like visits, observations.
	 * @param id
	 */
	void purge(String id);
}
