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
	void setLocationId(String locationId);
	
	public enum Status{
		Inpatient, Outpatient, Emergency
	}
}
