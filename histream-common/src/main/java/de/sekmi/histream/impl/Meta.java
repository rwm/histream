package de.sekmi.histream.impl;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.sekmi.histream.ObservationHandler;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.ext.ExternalSourceType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Meta {
//	@XmlElement(name="etl-strategy")
//	public String etlStrategy;
//	public ExternalSourceImpl source;
//	public Order order;
	/**
	 * ID for the source which provides the observations
	 */
	public static final String META_SOURCE_ID = "source.id";
	/**
	 * Timestamp when the source data was extracted/downloaded/queried
	 */
	public static final String META_SOURCE_TIMESTAMP = "source.timestamp";
	public static final String META_SOURCE_TIMEZONE = "source.timezone";
	
	/**
	 * Timestamp when the data has been uploaded
	 */
	public static final String META_UPLOAD_TIMESTAMP = "upload.timestamp";
	/**
	 * Strategy how to handle imported data. 
	 *  {@code replace-source} will drop any previous imports 
	 *  with the same {@code source.id}, {@code replace-visit} will 
	 *  delete any previous data with the same patient+visit combination.
	 */
	public static final String META_ETL_STRATEGY = "etl.strategy";
	/**
	 * If set to true, guarantees that all facts belonging to the same 
	 *  patient+visit combination are provided en bloc.
	 */
	public static final String META_ORDER_GROUPED = "order.grouped";
	/**
	 * If set to true, guarantees that all facts within the same
	 *  patient+visit combination occur in ascending order of start timestamp.
	 */
	public static final String META_ORDER_SORTED = "order.sorted";
	
	@XmlElement(name="property")
	public List<ScopedProperty> properties;

	public ExternalSourceType getSource() {
		ExternalSourceImpl source = new ExternalSourceImpl();
		if( properties == null ) {
			return null;
		}
		for( ScopedProperty p : properties ) {
			if( p.path != null ) {
				continue;
			}
			switch( p.name ) {
			case META_SOURCE_ID:
				source.setSourceId(p.value);
				break;
			case META_SOURCE_TIMESTAMP:
				source.setSourceTimestamp(javax.xml.bind.DatatypeConverter.parseDateTime(p.value).toInstant());
				break;
			case META_SOURCE_TIMEZONE:
				source.setSourceZone(ZoneId.of(p.value));
				break;
			}
		}
		return source;
	}
	public void setSource(ExternalSourceType source) {
		set(Meta.META_SOURCE_ID, source.getSourceId(), null);
		if( source.getSourceTimestamp() != null ) {
			Calendar cal = Calendar.getInstance(); // check if timezone is needed
			cal.setTimeInMillis(source.getSourceTimestamp().toEpochMilli());
			set(Meta.META_SOURCE_TIMESTAMP, javax.xml.bind.DatatypeConverter.printDateTime(cal), null);
		}else {
			
		}
		set(Meta.META_SOURCE_ID, source.getSourceId(), null);
	}

	public ScopedProperty remove(String key, String path) {
		Iterator<ScopedProperty> i = properties.iterator();
		while( i.hasNext() ) {
			ScopedProperty p = i.next();
			if( p.name.equals(key) && Objects.equals(p.path, path) ) {
				i.remove();
				return p;
			}
		}
		return null;
	}
	/**
	 * Set meta information via keys from {@link ObservationSupplier}
	 * @param key key
	 * @param value value
	 * @param path Scope path
	 */
	public void set(String key, String value, String path){
		if( this.properties == null ) {
			// lazy allocate
			this.properties = new ArrayList<>();
		}
//		switch( key ){
//		case ObservationSupplier.META_ETL_STRATEGY:
//			this.etlStrategy = value;
//			break;
//		case ObservationSupplier.META_SOURCE_TIMEZONE:
//			source.setSourceZone(ZoneId.of(value));
//			break;
//		case ObservationSupplier.META_SOURCE_ID:
//			if( source == null )source = new ExternalSourceImpl();
//			source.setSourceId(value);
//			break;
//		case ObservationSupplier.META_SOURCE_TIMESTAMP:
//			if( source == null )source = new ExternalSourceImpl();
//			source.setSourceTimestamp(javax.xml.bind.DatatypeConverter.parseDateTime(value).toInstant());
//			break;
//		}
		// TODO set
		ScopedProperty p = getProperty(key, path);
		if( p != null ) {
			// property already defined
			p.value = value;
		}else {
			properties.add(new ScopedProperty(path, key, value));
		}
	}
	
	public static final void transfer(ObservationSupplier source, ObservationHandler target){
//		String keys[] = new String[]{
//				ObservationSupplier.META_ETL_STRATEGY,
//				ObservationSupplier.META_SOURCE_ID,
//				ObservationSupplier.META_SOURCE_TIMEZONE,
//				ObservationSupplier.META_SOURCE_TIMESTAMP
//		};
//		for( String key : keys ){
//			String value = source.getMeta(key);
//			if( value == null )continue; // skip null values
//			target.setMeta(key, value, null);
//		}
		for( ScopedProperty prop : source.getMeta() ) {
			target.setMeta(prop.name, prop.value, prop.path);
		}
	}

	public ScopedProperty getProperty(String key, String path) {
		if( properties == null ) {
			return null;
		}
		for( ScopedProperty p : properties ) {
			if( p.name.equals(key) && Objects.equals(p.path, path) ) {
				return p;
			}
		}
		return null;
	}
	public String getValue(String key, String path) {
		ScopedProperty p = getProperty(key, path);
		if( p != null ) {
			return p.value;
		}else {
			return null;
		}
	}
}
