package de.sekmi.histream.ext;


import de.sekmi.histream.DateTimeAccuracy;

public interface Visit extends IdExtensionType,ExternalSourceType {
	DateTimeAccuracy getStartTime();
	
	/**
	 * End time of visit (patient gone)
	 * @return end time if available, null otherwise.
	 */
	DateTimeAccuracy getEndTime();

	void setEndTime(DateTimeAccuracy endTime);
	void setStartTime(DateTimeAccuracy startTime);
	
	Status getStatus();
	void setStatus(Status status);
	
	String getLocationId();
	
	public enum Status{
		Inpatient, Outpatient, Emergency
	}
}
