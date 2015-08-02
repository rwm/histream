package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;

public class IdatColumns {
	@XmlElement(name="patient-id")
	StringColumn patientId;
}
