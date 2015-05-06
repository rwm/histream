package de.sekmi.histream.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

import de.sekmi.histream.Plugin;

public class HIStream {

	private List<Plugin> plugins;
	
	public void loadPlugin(Constructor<? extends Plugin> constructor, Properties configuration) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		plugins.add(constructor.newInstance(configuration));
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
		// load plugins
		
		hs.shutdown();
	}
	
}
