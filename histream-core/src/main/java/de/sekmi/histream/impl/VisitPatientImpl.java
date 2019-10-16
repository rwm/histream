package de.sekmi.histream.impl;


import java.util.Objects;

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
import de.sekmi.histream.ext.StoredExtensionType;
import de.sekmi.histream.ext.Visit;

public class VisitPatientImpl extends StoredExtensionType implements Visit {
	private PatientImpl patient;
	private DateTimeAccuracy startTime;
	private DateTimeAccuracy endTime;
	private Status status;
	private String locationId;
	private String providerId;
	
	/**
	 * Empty constructor protected, only
	 * available to overriding classes.
	 */
	protected VisitPatientImpl() {
		
	}
	public VisitPatientImpl(String id, PatientImpl patient, DateTimeAccuracy startTime){
		setId(id);
		setPatient(patient);
		this.startTime = startTime;
	}
	
	public String getPatientId(){return patient.getId();}
	
	public void setPatient(PatientImpl patient){
		Objects.requireNonNull(patient);
		// patient id should not be changed normally.
		this.patient = patient;
		markDirty(true);
	}

	@Override
	public PatientImpl getPatient() {
		return this.patient;
	}
	@Override
	public DateTimeAccuracy getStartTime() {
		return startTime;
	}

	@Override
	public DateTimeAccuracy getEndTime() {
		return endTime;
	}

	@Override
	public Status getStatus() {
		return this.status;
	}

	@Override
	public void setStatus(Status status) {
		checkAndUpdateDirty(this.status, status);
		this.status = status;
	}

	@Override
	public String getLocationId() {
		return locationId;
	}
	
	@Override
	public void setLocationId(String locationId){
		checkAndUpdateDirty(this.locationId, locationId);
		this.locationId = locationId;
	}

	@Override
	public void setEndTime(DateTimeAccuracy endTime) {
		checkAndUpdateDirty(this.endTime, endTime);
		this.endTime = endTime;
	}

	@Override
	public void setStartTime(DateTimeAccuracy startTime) {
		checkAndUpdateDirty(this.startTime, startTime);
		this.startTime = startTime;
	}
	@Override
	public String getProviderId() {
		return this.providerId;
	}
	@Override
	public void setProviderId(String providerId) {
		checkAndUpdateDirty(this.providerId, providerId);
		this.providerId = providerId;
	}

	

}
