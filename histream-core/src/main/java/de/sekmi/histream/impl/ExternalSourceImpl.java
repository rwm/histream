package de.sekmi.histream.impl;

import java.time.Instant;

import de.sekmi.histream.ext.ExternalSourceType;

public class ExternalSourceImpl implements ExternalSourceType {
	
	protected Instant sourceTimestamp;
	protected String sourceId;


	@Override
	public Instant getSourceTimestamp() {
		return sourceTimestamp;
	}

	@Override
	public void setSourceTimestamp(Instant instant) {
		this.sourceTimestamp = instant;
	}

	@Override
	public String getSourceId() {
		return sourceId;
	}

	@Override
	public void setSourceId(String sourceSystemId) {
		this.sourceId = sourceSystemId;
	}
}
