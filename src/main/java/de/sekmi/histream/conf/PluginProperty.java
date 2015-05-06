package de.sekmi.histream.conf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "property")

public class PluginProperty {
    @XmlAttribute(required = true)
    protected String name;
    
    @XmlValue
    protected String value;

}
