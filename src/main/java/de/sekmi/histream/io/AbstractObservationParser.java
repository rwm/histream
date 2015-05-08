package de.sekmi.histream.io;

import java.time.Instant;

import de.sekmi.histream.ObservationFactory;

public class AbstractObservationParser {
	protected ObservationFactory factory;
	// meta
	protected Instant sourceTimestamp;
	protected String sourceId;
	protected String etlStrategy;

	public AbstractObservationParser(ObservationFactory factory){
		this.factory = factory;
	}
	
	protected void parseSourceTimestamp(String sourceTimestamp){
		this.sourceTimestamp = javax.xml.bind.DatatypeConverter.parseDateTime(sourceTimestamp).toInstant();
	}
	protected void setSourceId(String sourceId){
		this.sourceId = sourceId;
	}
	
	protected void setEtlStrategy(String strategy){
		this.etlStrategy = strategy;
	}
}
