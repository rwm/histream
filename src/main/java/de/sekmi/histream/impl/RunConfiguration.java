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


import java.io.Closeable;
import java.io.File;
import java.io.IOException;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import de.sekmi.histream.Extension;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationHandler;
import de.sekmi.histream.Plugin;
import de.sekmi.histream.conf.Configuration;
import de.sekmi.histream.conf.PluginConfig;
import de.sekmi.histream.conf.PluginRef;
import de.sekmi.histream.io.AbstractObservationParser;
import de.sekmi.histream.io.FileObservationProvider;
import de.sekmi.histream.io.FileObservationProviderFactory;

public class RunConfiguration implements Closeable{
	private static final Logger log = Logger.getLogger(RunConfiguration.class.getName());
	private ObservationFactory factory;
	private Plugin[] plugins;
	private FileObservationProviderFactory[] fileFactories;
	private Consumer<Observation> destinationChain;
	private ObservationHandler[] destinationHandlers;
	
	public RunConfiguration(Configuration conf) throws Exception{
		factory = new ObservationFactoryImpl();
		plugins = conf.createPluginInstances();
		
		List<FileObservationProviderFactory> ffs = new ArrayList<>();
		
		for(int i=0; i<plugins.length; i++ ){
			// register plugins
			if( plugins[i] instanceof Extension<?> ){
				// observation extension
				factory.registerExtension((Extension<?>)plugins[i]);
				// TODO log info
				log.info("Observation extension added: "+plugins[i]);
			}
			
			if( plugins[i] instanceof FileObservationProviderFactory ){
				// used to process files
				ffs.add((FileObservationProviderFactory)plugins[i]);
			}
		}
		
		if( ffs.size() > 0 ){
			fileFactories = ffs.toArray(new FileObservationProviderFactory[ffs.size()]);
		}
		
		// build destination chain
		buildDestinationChain(conf);
	}
	
	private int getPluginIndex(Configuration config, PluginConfig plugin){
		for( int i=0; i<config.getPlugins().length; i++ ){
			if( config.getPlugins()[i] == plugin )return i;
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	private void buildDestinationChain(Configuration conf){
		PluginRef[] ds = conf.getDestinations();
		ArrayList<ObservationHandler> a = new ArrayList<ObservationHandler>(ds.length);
		destinationChain = null;
		// chain subsequent destinations in order of configuration
		for( int i=0; i<ds.length; i++ ){
			Consumer<Observation> dest = (Consumer<Observation>)plugins[getPluginIndex(conf, ds[i].getPlugin())];
			if( destinationChain == null )destinationChain = dest;
			else destinationChain = destinationChain.andThen(dest);
			
			if( dest instanceof ObservationHandler ){
				a.add((ObservationHandler)dest);
			}
		}
		destinationHandlers = a.toArray(new ObservationHandler[a.size()]);

	}
		
	public void processFile(FileObservationProvider provider){
		for( ObservationHandler h : destinationHandlers ){
			h.setMeta("etl.strategy", provider.getMeta("etl.strategy"));
		}
		AbstractObservationParser.nonNullStream(provider).forEach(destinationChain);
	}
	
	public FileObservationProvider providerForFile(File file){
		FileObservationProvider p = null;
		for( int i=0; i<fileFactories.length; i++ ){
			try {
				p = fileFactories[i].forFile(file, factory);
				break;
			} catch (IOException e) {
				// unable to process file
			}
		}
		return p;
	}
	
	private static final String readVersion() throws IOException{
		InputStream inputStream = RunConfiguration.class.getClassLoader().getResourceAsStream("META-INF/maven/de.sekmi.histream/histream/pom.properties");
		Properties props = new Properties();
		props.load(inputStream);
		return props.getProperty("version","[unknown]");
	}
	public static void main(String args[])throws Exception{
		if( System.console() == null ){
			JOptionPane.showMessageDialog(null, "This program should be run with a console", "HIStream "+readVersion(), JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		System.out.println("HIStream "+readVersion()+" starting");
		long millis = System.currentTimeMillis();
		
		Configuration conf = Configuration.fromFile(new File("src/test/resources/histream.xml"));
		RunConfiguration rc = new RunConfiguration(conf);

		// TODO set error handlers for destinations
		
		// if listeners specified, run as server (don't exit)
		args = new String[]{
				"src/test/resources/dwh-eav.xml",
				"src/test/resources/dwh-flat.txt"
				};
		
		if( args.length > 0 ){
			for( int i=0; i<args.length; i++ ){
				File file = new File(args[i]);
				FileObservationProvider p = rc.providerForFile(file);
				if( p != null ){
					System.out.println("ETL("+p.getMeta("etl.strategy")+"): "+file);
					rc.processFile(p);
				}else{
					System.err.println("Unable to find parser for file "+file);
				}
			}
			// files specified, run in batch mode
		}
		
		rc.close();
		float duration = (System.currentTimeMillis() - millis)/1000f;
		System.out.println("HIStream finished ("+duration+"s)");
	}

	@Override
	public void close() throws IOException {
		for(int i=0; i<plugins.length; i++ ){
			plugins[i].close();
		}
	}
}
