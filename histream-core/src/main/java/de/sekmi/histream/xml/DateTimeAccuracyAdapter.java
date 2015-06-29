package de.sekmi.histream.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.sekmi.histream.DateTimeAccuracy;

/**
 * Serialize {@link DateTimeAccuracy} to/from a partial ISO8601 string
 * 
 * @author marap1
 *
 */
public class DateTimeAccuracyAdapter extends XmlAdapter<String, DateTimeAccuracy>{

	@Override
	public DateTimeAccuracy unmarshal(String v) {
		if( v == null )return null;
		return DateTimeAccuracy.parsePartialIso8601(v);
	}

	@Override
	public String marshal(DateTimeAccuracy v) {
		if( v == null )return null;
		return v.toPartialIso8601();
	}

}
