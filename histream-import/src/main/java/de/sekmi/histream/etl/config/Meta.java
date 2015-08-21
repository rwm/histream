package de.sekmi.histream.etl.config;

import java.time.Instant;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import de.sekmi.histream.ext.ExternalSourceType;

public class Meta {
	@XmlElement
	Source source;
	
	@XmlElement(name="etl-strategy")
	String etlStrategy;

	public static class Source implements ExternalSourceType{
		@XmlAttribute
		String id;

		@XmlAttribute
		Calendar timestamp;

		@Override
		public String getSourceId() {return id;}

		@Override
		public Instant getSourceTimestamp() {
			return timestamp.toInstant();
		}

		@Override
		public void setSourceId(String arg0) {this.id = arg0;}

		@Override
		public void setSourceTimestamp(Instant instant) {
			this.timestamp = Calendar.getInstance();
			this.timestamp.setTimeInMillis(instant.toEpochMilli());
		}
	}
	
	protected Meta(){
	}
	
	public Meta(String etlStrategy, String sourceId, Calendar sourceTimestamp){
		this.etlStrategy = etlStrategy;
		this.source = new Source();
		this.source.timestamp = sourceTimestamp;
		this.source.id = sourceId;
	}
	public Source getSource(){
		return this.source;
	}
}
