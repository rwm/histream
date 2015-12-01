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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Logger;

import de.sekmi.histream.Extension;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ObservationHandler;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.Plugin;
import de.sekmi.histream.conf.Configuration;
import de.sekmi.histream.conf.PluginConfig;
import de.sekmi.histream.conf.PluginRef;
import de.sekmi.histream.impl.AbstractObservationHandler;
import de.sekmi.histream.io.FileObservationSupplierFactory;
import de.sekmi.histream.io.Streams;

public class RunConfiguration implements Closeable{
	private static final Logger log = Logger.getLogger(RunConfiguration.class.getName());
	private ObservationFactory factory;
	private Plugin[] plugins;
	private FileObservationSupplierFactory[] fileFactories;
	private Consumer<Observation> destinationChain;
	private ObservationHandler[] destinationHandlers;
	private Consumer<ObservationException> errorHandler;
	
	public RunConfiguration(Configuration conf) throws Exception{
		factory = new ObservationFactoryImpl();
		plugins = conf.createPluginInstances();
		errorHandler = new Consumer<ObservationException>() {
			@Override
			public void accept(ObservationException t) {
				System.err.println("Error:"+t.getMessage());
			}
		};
		
		List<FileObservationSupplierFactory> ffs = new ArrayList<>();
		
		for(int i=0; i<plugins.length; i++ ){
			// register plugins
			if( plugins[i] instanceof Extension<?> ){
				// observation extension
				factory.registerExtension((Extension<?>)plugins[i]);
				// TODO log info
				log.info("Observation extension added: "+plugins[i]);
			}
			
			if( plugins[i] instanceof FileObservationSupplierFactory ){
				// used to process files
				ffs.add((FileObservationSupplierFactory)plugins[i]);
			}
		}
		
		if( ffs.size() > 0 ){
			fileFactories = ffs.toArray(new FileObservationSupplierFactory[ffs.size()]);
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
			if( dest instanceof AbstractObservationHandler ){
				((AbstractObservationHandler)dest).setErrorHandler(errorHandler);
			}
			if( destinationChain == null )destinationChain = dest;
			else destinationChain = destinationChain.andThen(dest);
			
			if( dest instanceof ObservationHandler ){
				a.add((ObservationHandler)dest);
			}
		}
		destinationHandlers = a.toArray(new ObservationHandler[a.size()]);

	}
		
	public void processFile(ObservationSupplier provider){
		String etlStrategy = provider.getMeta(ObservationSupplier.META_ETL_STRATEGY);
		if( etlStrategy != null ){
			for( ObservationHandler h : destinationHandlers ){
				h.setMeta(ObservationSupplier.META_ETL_STRATEGY, etlStrategy);
			}			
		}
		Streams.nonNullStream(provider).forEach(destinationChain);
	}
	
	public ObservationSupplier providerForFile(File file)throws IOException{
		ObservationSupplier p = null;
		// if only one file factory present, pass on exception
		if( fileFactories.length == 1 ){
			p = fileFactories[0].forFile(file, factory);
		}else{
			// multiple file factories, find one which doesn't give errors
			for( int i=0; i<fileFactories.length; i++ ){
				try {
					p = fileFactories[i].forFile(file, factory);
					break;
				} catch (IOException e) {
					// unable to process file
				}
			}			
		}
		return p;
	}
	
	private static final String readVersion() throws IOException{
		InputStream inputStream = RunConfiguration.class.getClassLoader().getResourceAsStream("META-INF/maven/de.sekmi.histream/histream-core/pom.properties");
		String version;
		if( inputStream != null ){
			Properties props = new Properties();
			props.load(inputStream);
			inputStream.close();
			version = props.getProperty("version","[unknown]");
		}else{
			// file is not run from jar
			// try to get the version somewhere else
			version = "[unknown]";
		}
		return version;
	}
	public static void main(String args[])throws Exception{
		
		/*
		if( System.console() == null ){
			JOptionPane.showMessageDialog(null, "This program should be run with a console", "HIStream "+readVersion(), JOptionPane.WARNING_MESSAGE);
			return;
		}*/
		// TODO check for file histream.xml
		File xml = new File("histream.xml");
		if( !xml.canRead() ){
			System.err.println("Unable to find/read file histream.xml");
			xml = new File("examples/histream.xml");
			if( xml.canRead() )
				System.err.println("Using src/test/resources/histream.xml");
			else
				xml = null;
		}
		
		if( xml == null ){
			System.err.println("Unable to run without configuration");
			System.exit(1);
		}
		
		readFiles(xml, args);

	}
	public static void readFiles(File xml, String files[])throws Exception{		
		System.out.println("HIStream "+readVersion()+" starting");
		Instant begin = Instant.now();
		
		Configuration conf = Configuration.fromFile(xml);
		RunConfiguration rc = new RunConfiguration(conf);

		// TODO set error handlers for destinations
		
		// if listeners specified, run as server (don't exit)
		
		if( files.length > 0 ){
			for( int i=0; i<files.length; i++ ){
				File file = new File(files[i]);
				ObservationSupplier p = rc.providerForFile(file);
				if( p != null ){
					System.out.println("ETL("+p.getMeta("etl.strategy")+"): "+file);
					rc.processFile(p);
					p.close();
				}else{
					System.err.println("ERROR: Unable to find parser for file "+file);
					System.err.println("exceptions reported by all "+rc.fileFactories.length+" registered parsers");
					System.err.println("for detailed errors, use only a single parser");
				}
			}
			// files specified, run in batch mode
		}
		
		rc.close();
		Duration duration = Duration.between(begin, Instant.now());
		System.out.println("HIStream finished (duration "+duration.toString()+")");
	}

	@Override
	public void close() throws IOException {
		for(int i=0; i<plugins.length; i++ ){
			plugins[i].close();
		}
	}
}
