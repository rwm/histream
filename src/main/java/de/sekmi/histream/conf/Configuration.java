package de.sekmi.histream.conf;

import java.io.File;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Configuration for HIStream processing. Uses javax.xml.bind for loading XML files
 * via 
 * @author Raphael
 *
 */
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
    protected PluginRef[] source;
    @XmlElement(required = true)
    protected PluginRef[] destination;

    
    public PluginConfig[] getPlugins(){return plugins;}
    public PluginRef[] getDestinations(){return destination;}
    public PluginRef[] getSources(){return source;}
    
    
    public static final Configuration fromFile(File file){
    	return JAXB.unmarshal(file, Configuration.class);
    }
}
