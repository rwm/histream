package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;

public class Meta {
	@XmlElement
	String id;

	@XmlElement(name="etl-strategy")
	String etlStrategy;

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

}
