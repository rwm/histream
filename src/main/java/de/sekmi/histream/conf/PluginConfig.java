package de.sekmi.histream.conf;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

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
    
    @SuppressWarnings("unchecked")
	public Plugin newInstance() throws Exception{
		Constructor<? extends Plugin> c;
		try {
			c = (Constructor<? extends Plugin>) resolveClass().getConstructor(Map.class);
		} catch (NoSuchMethodException | SecurityException| ClassNotFoundException e) {
			throw new Exception("Unable to find constructor",e);
		}
    	HashMap<String, String> props = new HashMap<>();
    	if( property != null ){
	    	for( PluginProperty prop : property ){
	    		props.put(prop.name, prop.value);
	    	}
    	}
    	return c.newInstance(props);
    }
}
