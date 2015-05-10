package de.sekmi.histream.conf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pluginref")
public class PluginRef {
    @XmlAttribute(name = "plugin", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected PluginConfig plugin;
    
    /**
     * Returns a reference to a plugin configuration.
     * @return plugin configuration
     */
    public PluginConfig getPlugin(){return plugin;}

}
