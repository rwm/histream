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

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.sekmi.histream.Plugin;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "plugin")
public class PluginConfig {
    @XmlAttribute(name = "class", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String clazz;

    @XmlElement
    protected PluginProperty[] property;

    public Class<?> resolveClass() throws ClassNotFoundException{
    	return Class.forName(clazz);
    }
    
    public String getClazz(){ return clazz; }
    
	public Plugin newInstance() throws Exception{
    	HashMap<String, String> props = new HashMap<>();
    	if( property != null ){
	    	for( PluginProperty prop : property ){
	    		props.put(prop.name, prop.value);
	    	}
    	}
    	return Plugin.newInstance(resolveClass(), props);
    }
}
