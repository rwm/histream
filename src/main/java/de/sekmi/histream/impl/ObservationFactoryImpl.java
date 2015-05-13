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


import java.util.Arrays;
import java.util.Hashtable;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Extension;
import de.sekmi.histream.ExtensionAccessor;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;

public class ObservationFactoryImpl implements ObservationFactory{

	private static class ExtensionEntry{
		public Extension<?> extension;
		public int index;
		
		public ExtensionEntry(Extension<?> extension, int index){
			this.extension = extension;
			this.index =index;
		}
	}
	
	private Hashtable<Class<?>, ExtensionEntry> extensions;
	
	

	public ObservationFactoryImpl() {
		extensions = new Hashtable<>();
	}	
	
	@Override
	public void registerExtension(Extension<?> extension) {
		// make sure hashtable is not modified between index calculation and assignment
		synchronized( extensions ){
			for( Class<?> type : extension.getInstanceTypes() ){
				if( extensions.containsKey(type) )
					throw new IllegalArgumentException("Extension type "+type+" already registered");
			}
			int index = extensions.size();
		
			ExtensionEntry entry =  new ExtensionEntry(extension, index);
			for( Class<?> type : extension.getInstanceTypes() )
				extensions.put(type, entry);
		}
	}

	/**
	 * Get the extension type instance for a given observation. Returns a cached
	 * object, if the instance was already requested before. Otherwise, a new
	 * instance is generated with the help of the extension.
	 * 
	 * @param observation observation for which the extension type is requested.
	 * @param extensionType extension type to request. E.g. the type supplied to the subclass of Extension. 
	 * @return extension type instance
	 */
	protected <T> T getExtension(ObservationImpl observation, Class<T> extensionType) {
		ExtensionEntry e = extensions.get(extensionType);
		if( e == null )throw new IllegalArgumentException("Unsupported extension type "+extensionType);
		return getExtension(observation, e);
	}
	
	/**
	 * Set extension
	 * @param observation
	 * @param extensionType
	 * @param extension
	 * @throws IllegalArgumentException if extension type is not part of the registered extensions
	 */
	protected <T> void setExtension(ObservationImpl observation, Class<T> extensionType, T extension)throws IllegalArgumentException {
		ExtensionEntry e = extensions.get(extensionType);
		if( e == null )throw new IllegalArgumentException("Unsupported extension type "+extensionType);
		setExtension(observation, e, extension);
	}
	
	private void lazyAllocateArray(ObservationImpl observation){
		int extensionCount = extensions.size();
		if( observation.extensions == null ){
			// lazy allocate array
			observation.extensions = new Object[extensionCount];
		}else if( observation.extensions.length < extensionCount ){
			// extension was added at a later time, resize the array
			observation.extensions = Arrays.copyOf(observation.extensions, extensionCount);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getExtension(ObservationImpl observation, ExtensionEntry entry) {
		lazyAllocateArray(observation);
		// retrieve cached object
		T obj = (T) observation.extensions[entry.index];
		
		if( obj == null ){
			// if accessed for the first time, create instance
			obj = (T) entry.extension.createInstance(observation);
			// and put in cache
			observation.extensions[entry.index] = obj; 
		}

		return obj;
	}
	
	private <T> void setExtension(ObservationImpl observation, ExtensionEntry entry, T ext){
		lazyAllocateArray(observation);
		observation.extensions[entry.index] = ext;
	}
	/*

	@Override
	public Iterable<Extension<?>> registeredExtensions() {
		return new Iterable<Extension<?>>() {
			@Override
			public Iterator<Extension<?>> iterator() {
				final Iterator<ExtensionEntry> parent = extensions.values().iterator();
				return new Iterator<Extension<?>>() {
					@Override
					public boolean hasNext() {return parent.hasNext();}

					@Override
					public Extension<?> next() {
						ExtensionEntry entry = parent.next();
						if( entry != null )return entry.extension;
						else return null;
					}

					@Override
					public void remove() {parent.remove();}
				};
			}
		};
	}*/

	@Override
	public ObservationImpl createObservation(String patientId, String conceptId, DateTimeAccuracy startTime) {
		ObservationImpl impl = new ObservationImpl(this);
		impl.setPatientId(patientId);
		impl.setConceptId(conceptId);
		impl.setStartTime(startTime);
		return impl;
	}

	@Override
	public <T> ExtensionAccessor<T> getExtensionAccessor(final Class<T> extensionType) {
		final ExtensionEntry entry = extensions.get(extensionType);
		if( entry == null )return null;
		return new ExtensionAccessor<T>() {

			@Override
			public T access(Observation observation) {
				return getExtension((ObservationImpl)observation, entry);
			}

			@Override
			public void set(Observation observation, T ext) {
				setExtension((ObservationImpl)observation, entry, ext);
			}
		};
	}

}
