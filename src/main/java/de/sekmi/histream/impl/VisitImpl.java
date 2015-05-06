package de.sekmi.histream.impl;


import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.ext.StoredExtensionType;
import de.sekmi.histream.ext.Visit;

public class VisitImpl extends StoredExtensionType implements Visit {
	private DateTimeAccuracy startTime;
	private DateTimeAccuracy endTime;
	private Status status;
	private String patientId;
	private String locationId;
	
	public VisitImpl(){
		
	}
	
	public VisitImpl(String id, String patientId, DateTimeAccuracy startTime, DateTimeAccuracy endTime, Status status){
		setId(id);
		this.patientId = patientId;
		this.status = status;
		this.startTime = startTime;
		this.endTime = endTime;
		markDirty(true);
	}

	public String getPatientId(){return patientId;}
	
	public void setPatientId(String patientId){this.patientId = patientId;}
	
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
		this.status = status;
		markDirty(true); // maybe compare first
	}

	@Override
	public String getLocationId() {
		return locationId;
	}
	
	public void setLocationId(String locationId){
		this.locationId = locationId;
		markDirty(true);
	}

	@Override
	public void setEndTime(DateTimeAccuracy endTime) {
		this.endTime = endTime;
		markDirty(true);
	}

	@Override
	public void setStartTime(DateTimeAccuracy startTime) {
		this.startTime = startTime;
		markDirty(true);
	}

	

}
