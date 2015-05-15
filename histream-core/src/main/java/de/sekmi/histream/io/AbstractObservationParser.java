package de.sekmi.histream.io;

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


import java.time.Instant;
import java.util.Hashtable;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;

public class AbstractObservationParser {
	protected ObservationFactory factory;
	// meta
	protected Instant sourceTimestamp;
	protected String sourceId;
	protected String etlStrategy;
	private Map<String,String> meta;

	public AbstractObservationParser(){
		meta = new Hashtable<>();	
	}
	
	/**
	 * Set meta information for this parser
	 * @param key meta key
	 * @param value meta value
	 */
	protected void setMeta(String key, String value){
		if( value == null ){
			// clear value, remove key
			meta.remove(key);
		}else{
			meta.put(key, value);
		}
		switch( key ){
		case "source.timestamp":
			if( value == null )this.sourceTimestamp = null;
			else this.sourceTimestamp = javax.xml.bind.DatatypeConverter.parseDateTime(value).toInstant();
			break;
		case "source.id":
			this.sourceId = value;
			break;
		case "etl.strategy":
			this.etlStrategy = value;
			break;
		}

	}
	
	public String getMeta(String key){
		return meta.get(key);
	}
	
	public void setObservationFactory(ObservationFactory factory){
		this.factory = factory;
	}
	
	public static Spliterator<Observation> nonNullSpliterator(Supplier<Observation> supplier){
		return new NonNullSpliterator(supplier);
	}
	public static Stream<Observation> nonNullStream(Supplier<Observation> supplier){
		return StreamSupport.stream(new NonNullSpliterator(supplier), false);
	}
	
	private static class NonNullSpliterator implements Spliterator<Observation>{
		private Supplier<Observation> supplier;
		
		public NonNullSpliterator(Supplier<Observation> supplier) {
			this.supplier = supplier;
		}
		@Override
		public boolean tryAdvance(Consumer<? super Observation> action) {
			Observation o = supplier.get();
			if( o == null )return false;
			action.accept(o);
			return true;
		}

		@Override
		public Spliterator<Observation> trySplit() {
			return null;
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public int characteristics() {
			return Spliterator.NONNULL | Spliterator.IMMUTABLE;
		}
		
	}
}
