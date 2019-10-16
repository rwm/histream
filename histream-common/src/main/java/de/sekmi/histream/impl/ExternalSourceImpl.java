package de.sekmi.histream.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.sekmi.histream.ext.ExternalSourceType;

@XmlRootElement(name="source")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ExternalSourceImpl implements ExternalSourceType {
	private Instant timestamp;
	private String id;
	private ZoneId timezone;

	/**
	 * Empty constructor for JAXB
	 */
	public ExternalSourceImpl(){
	}
	/**
	 * Create external source
	 * @param id id string
	 * @param timestamp timestamp
	 */
	public ExternalSourceImpl(String id, Instant timestamp){
		this();
		this.id = id;
		this.timestamp = timestamp;
	}
	
	private static class Adapter extends XmlAdapter<String, Instant>{
		@Override
		public Instant unmarshal(String v) throws Exception {
			if( v == null )return null;
			return javax.xml.bind.DatatypeConverter.parseDateTime(v).toInstant();
		}

		@Override
		public String marshal(Instant v) throws Exception {
			if( v == null )return null;
			return v.toString();
		}
	}
	private static class ZoneAdapter extends XmlAdapter<String, ZoneId>{
		@Override
		public ZoneId unmarshal(String v) throws Exception {
			if( v == null )return null;
			return ZoneId.of(v);
		}

		@Override
		public String marshal(ZoneId v) throws Exception {
			if( v == null )return null;
			return v.getId();
		}
	}

	@XmlAttribute(name="timestamp")
	@XmlJavaTypeAdapter(Adapter.class)
	@Override
	public Instant getSourceTimestamp() {
		return timestamp;
	}

	@Override
	public void setSourceTimestamp(Instant instant) {
		this.timestamp = instant;
	}

	@XmlAttribute(name="timezone")
	@XmlJavaTypeAdapter(ZoneAdapter.class)
	public ZoneId getSourceZone() {
		return timezone;
	}

	public void setSourceZone(ZoneId zone) {
		this.timezone = zone;
	}

	@XmlAttribute(name="id")
	@Override
	public String getSourceId() {
		return id;
	}

	@Override
	public void setSourceId(String sourceSystemId) {
		this.id = sourceSystemId;
	}

	@Override
	public boolean equals(Object other) {
		if( !(other instanceof ExternalSourceImpl) ) {
			return false;
		}
		ExternalSourceImpl s = (ExternalSourceImpl)other;
		return Objects.equals(this.id, s.id) 
				&& Objects.equals(this.timestamp, s.timestamp) 
				&& Objects.equals(this.timezone, s.timezone);
	}
	
}
