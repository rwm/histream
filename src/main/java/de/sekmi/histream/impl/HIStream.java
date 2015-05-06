package de.sekmi.histream.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;



import javax.xml.bind.JAXB;

import de.sekmi.histream.Plugin;
import de.sekmi.histream.conf.Configuration;

public class HIStream {

	private List<Plugin> plugins;
	
	public void loadPlugin(Constructor<? extends Plugin> constructor, Properties configuration) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		plugins.add(constructor.newInstance(configuration));
	}
	
	public HIStream(){
		plugins = new LinkedList<>();
	}
	public void shutdown(){
		for( Plugin plugin : plugins ){
			try {
				plugin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		HIStream hs = new HIStream();
		// load configuration
		Configuration config = JAXB.unmarshal(new File("src/test/resources/histream.xml"), Configuration.class);
		System.out.println("Configuration with "+config.getPlugins().length+" plugins");

		// load plugins
		
		hs.shutdown();
	}
	
}
