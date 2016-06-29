package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

public class EavTable extends AbstractTable{

	@XmlID
	@XmlAttribute
	String id;
	
	@XmlIDREF
	@XmlAttribute(name="class")
	ConceptGroup clazz;
}
