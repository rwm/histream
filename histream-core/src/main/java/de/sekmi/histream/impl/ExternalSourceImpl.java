package de.sekmi.histream.impl;

import java.time.Instant;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.sekmi.histream.ext.ExternalSourceType;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ExternalSourceImpl implements ExternalSourceType {
	private Instant timestamp;
	private String id;

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

	@XmlAttribute(name="id")
	@Override
	public String getSourceId() {
		return id;
	}

	@Override
	public void setSourceId(String sourceSystemId) {
		this.id = sourceSystemId;
	}
}
