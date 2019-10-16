package de.sekmi.histream.etl.config;

import java.net.URL;
import java.time.ZoneId;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
/**
 * Meta information.
 * 
 * @author R.W.Majeed
 *
 */
public class Meta {
	@XmlElement
	String id;

	@XmlElement(name="etl-strategy")
	String etlStrategy;

	@XmlElement
	String timezone;

	@XmlTransient
	private URL location;
	
	@XmlTransient
	private Long lastModified;
	
	protected Meta(){
	}

	public Meta(String etlStrategy, String sourceId){
		this.etlStrategy = etlStrategy;
		this.id = sourceId;
	}
	public String getSourceId(){
		return this.id;
	}

	public String getETLStrategy(){
		return etlStrategy;
	}
	
	public ZoneId getTimezone() {
		if( timezone == null ) {
			return null;
		}else {
			return ZoneId.of(timezone);
		}
	}
	public URL getLocation(){
		return location;
	}
	public void setLocation(URL location){
		this.location = location;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	public Long getLastModified(){
		return this.lastModified;
	}

}
