package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlElement;

public class PatientTable {

	@XmlElement(name="column")
	AbstractColumn[] columns;
}
