package de.sekmi.histream.ext;

import de.sekmi.histream.DateTimeAccuracy;

public interface Patient extends IdExtensionType, ExternalSourceType{
	/**
	 * Birth date
	 * @return birth date with accuracy
	 */
	DateTimeAccuracy getBirthDate();
	void setBirthDate(DateTimeAccuracy birthDate);
	
	/**
	 * Death date.
	 * @return death date if available, null otherwise
	 */
	DateTimeAccuracy getDeathDate();
	void setDeathDate(DateTimeAccuracy deathDate);
	
	/**
	 * Get the patient's biological sex. 
	 * @return Male, Female or null if unknown.
	 */
	Sex getSex();
	void setSex(Sex sex);
	
	public enum Sex{
		Female, Male
	}
}
