package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;

public class Meta {
	@XmlElement(name="source-id")
	String sourceId;

	@XmlElement(name="etl-strategy")
	String etlStrategy;
}
