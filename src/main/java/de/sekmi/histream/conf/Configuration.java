package de.sekmi.histream.conf;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://some/namespace",
		name = "histream", propOrder = {
	    "plugins",
	    "source",
	    "destination"
	})
public class Configuration {
    @XmlElement(name="plugin")
    @XmlElementWrapper(name="plugins", required=true)
    protected PluginConfig[] plugins;
    @XmlElement(required = true)
    protected List<PluginRef> source;
    @XmlElement(required = true)
    protected List<PluginRef> destination;

    
    public PluginConfig[] getPlugins(){return plugins;}
}
