package de.sekmi.histream.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.sekmi.histream.ObservationHandler;
import de.sekmi.histream.ObservationSupplier;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Meta {
	@XmlElement(name="etl-strategy")
	public String etlStrategy;
	public ExternalSourceImpl source;
	public Order order;
	
	public static class Order{
		@XmlAttribute
		Boolean grouped;
		@XmlAttribute
		Boolean sorted;
		private Order(){}
		public Order(Boolean grouped, Boolean sorted){
			this();
			this.grouped = grouped;
			this.sorted = sorted;
		}
	}
	
	/**
	 * Set meta information via keys from {@link ObservationSupplier}
	 * @param key key
	 * @param value value
	 */
	public void set(String key, String value){
		switch( key ){
		case ObservationSupplier.META_ETL_STRATEGY:
			this.etlStrategy = value;
			break;
		case ObservationSupplier.META_SOURCE_ID:
			if( source == null )source = new ExternalSourceImpl();
			source.setSourceId(value);
			break;
		case ObservationSupplier.META_SOURCE_TIMESTAMP:
			if( source == null )source = new ExternalSourceImpl();
			source.setSourceTimestamp(javax.xml.bind.DatatypeConverter.parseDateTime(value).toInstant());
			break;
		}
	}
	
	public static final void transfer(ObservationSupplier source, ObservationHandler target){
		String keys[] = new String[]{
				ObservationSupplier.META_ETL_STRATEGY,
				ObservationSupplier.META_SOURCE_ID,
				ObservationSupplier.META_SOURCE_TIMESTAMP
		};
		for( String key : keys ){
			String value = source.getMeta(key);
			if( value == null )continue; // skip null values
			target.setMeta(key, value);
		}
	}

	public String get(String key) {
		switch( key ){
		case ObservationSupplier.META_ETL_STRATEGY:
			return this.etlStrategy;
		case ObservationSupplier.META_SOURCE_ID:
			if( source == null )return null;
			else return source.getSourceId();
		case ObservationSupplier.META_SOURCE_TIMESTAMP:
			if( source == null )return null;
			else if( source.getSourceTimestamp() == null )return null;
			else return source.getSourceTimestamp().toString();
		}
		return null;
	}
}
