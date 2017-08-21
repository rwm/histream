package de.sekmi.histream.xml;

import java.text.ParseException;
import java.time.ZoneId;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.sekmi.histream.DateTimeAccuracy;

/**
 * Serialize {@link DateTimeAccuracy} to/from a partial ISO8601 string
 * 
 * @author marap1
 *
 */
public class DateTimeAccuracyAdapter extends XmlAdapter<String, DateTimeAccuracy>{
	private ZoneId zoneId;

	/**
	 * Specify a local time zone. If non-{@code null}, the result of {@link #marshal(DateTimeAccuracy)}
	 * will always include the offset of the specified zone.
	 * When unmarshalling a string without offset, then the timestamp is treated as if it was
	 * in the specified zone id.
	 * @param zoneId local zone id to use as default for both {@link #marshal(DateTimeAccuracy)} and {@link #unmarshal(String)}
	 */
	public void setZoneId(ZoneId zoneId){
		this.zoneId = zoneId;
	}

	@Override
	public DateTimeAccuracy unmarshal(String v) throws ParseException {
		if( v == null )return null;
		// parsing will support any zone offset
		// TODO if zone is missing, assume specified zone
		return DateTimeAccuracy.parsePartialIso8601(v);
	}

	@Override
	public String marshal(DateTimeAccuracy v) {
		if( v == null )return null;
		return v.toPartialIso8601(zoneId);
	}

}
