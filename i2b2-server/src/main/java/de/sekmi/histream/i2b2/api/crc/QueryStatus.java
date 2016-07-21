package de.sekmi.histream.i2b2.api.crc;

public enum QueryStatus {
	ERROR, COMPLETED(6), FINISHED(3), INCOMPLETE, WAITTOPROCESS, PROCESSING;
	
	int typeId;
	private QueryStatus(int typeId){
		this.typeId = typeId;
	}
	private QueryStatus(){
		this.typeId = 0;
	}
}
