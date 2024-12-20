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


import de.sekmi.histream.DateTimeAccuracy;

/**
 * Interface for patient information.
 * 
 * @author R.W.Majeed
 *
 */
public interface Patient {
	String getId();
	ExternalSourceType getSource();
	void setSource(ExternalSourceType source);
	/**
	 * Birth date
	 * @return birth date with accuracy
	 */
	DateTimeAccuracy getBirthDate();
	void setBirthDate(DateTimeAccuracy birthDate);
	
	/**
	 * Death date. Note that the patient might be known deceased, 
	 * even if a death date is not available. To find that out,
	 * use {@link #getDeceased()}
	 * 
	 * @return death date if available, null otherwise
	 * @see #getDeceased()
	 */
	DateTimeAccuracy getDeathDate();
	void setDeathDate(DateTimeAccuracy deathDate);
	
	/**
	 * Determine whether the patient is known to be deceased.
	 * @return true/false or {@code null} if unknown
	 */
	Boolean getDeceased();
	/**
	 * Set the patient's vital status.
	 * @param deceased true for known deceased, false for known living, null for unknown
	 */
	void setDeceased(Boolean deceased);
	
	/**
	 * Get the patient's biological sex. 
	 * @return Male, Female or null if unknown.
	 */
	Sex getSex();
	void setSex(Sex sex);
	
	String getSurname();
	void setSurname(String surname);
	
	String getGivenName();
	void setGivenName(String givenName);
	
	public enum Sex{
		female, male, indeterminate
	}
}
