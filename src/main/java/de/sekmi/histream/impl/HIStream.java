package de.sekmi.histream.impl;

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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;





import javax.xml.bind.JAXB;

import de.sekmi.histream.Plugin;
import de.sekmi.histream.conf.Configuration;
import de.sekmi.histream.conf.PluginConfig;

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
		for( PluginConfig p : config.getPlugins() ){
			try {
				Class<?> c = p.resolveClass();
				System.out.println("Plugin class loaded: "+c);
			} catch (ClassNotFoundException e) {
				System.err.println("Plugin class not found: "+p.getClazz());
			}
		}
		// load plugins
		
		hs.shutdown();
	}
	
}
