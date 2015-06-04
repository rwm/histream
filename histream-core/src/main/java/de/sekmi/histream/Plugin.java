package de.sekmi.histream;

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


import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


/**
 * 
 * <p>
 * A Plugin should implement a constructor accepting a single 
 * {@link Map} object for configuration. The {@link Closeable#close()}
 * method is called when the plugin is unloaded or during normal termination.
 * 
 * @author Raphael
 *
 */
public interface Plugin extends Closeable{


	@Override
	default void close()throws IOException{}
	
	@SuppressWarnings("unchecked")
	static Plugin newInstance(Class<?> pluginClass, Map<String,String> configuration)
			throws NoSuchMethodException,SecurityException,ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Constructor<? extends Plugin> c;
		c = (Constructor<? extends Plugin>) pluginClass.getConstructor(Map.class);
		
    	return c.newInstance(configuration);

	}
}
