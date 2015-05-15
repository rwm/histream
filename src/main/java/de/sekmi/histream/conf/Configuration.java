package de.sekmi.histream.conf;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.Plugin;


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
	private static final Logger log = Logger.getLogger(Configuration.class.getName());
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
    
    
    /**
     * Instantiates each plugin listed order (as specified in configuration).
     * Instantiation is performed by looking up a constructor which accepts a single
     * argument of type {@link Map} and calling that constructor with the properties
     * specified for the plugin.
     * <p>
     * If an exception is thrown by the constructor if any plugin, the previously instantiated
     * plugins are closed in reverse order. Any exceptions thrown by the close methods are suppressed
     * by the constructor exception (via {@link Exception#addSuppressed(Throwable)}).
     * @return plugin instances
     * @throws Exception exception thrown by any plugin during construction
     */
    public Plugin[] createPluginInstances() throws Exception{
    	Plugin[] insts = new Plugin[plugins.length];
    	for( int i=0; i<plugins.length; i++ ){
    		try{
    			insts[i] = plugins[i].newInstance();
    			log.info("Plugin instance created: "+insts[i]);
    		}catch( Exception e ){
    			// close previously instantiated plugins in reverse order
    			for( int j=i-1; j>=0; j-- ){
    				try{
    					insts[j].close();
    				}catch( IOException f ){
    					e.addSuppressed(f);
    				}
    			}
    			throw e;
    		}
    	}
    	return insts;
    }
}
