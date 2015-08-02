package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;

public class DataTableIdat extends IdatColumns {
	@XmlElement(name="visit-id")
	StringColumn visitId;
}
