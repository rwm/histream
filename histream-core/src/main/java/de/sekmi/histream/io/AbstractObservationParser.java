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

import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ext.ExternalSourceType;

public class AbstractObservationParser implements ExternalSourceType{
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

	@Override
	public Instant getSourceTimestamp() {
		return sourceTimestamp;
	}

	@Override
	public void setSourceTimestamp(Instant sourceTimestamp) {
		this.sourceTimestamp = sourceTimestamp;
	}

	@Override
	public String getSourceId() {
		return sourceId;
	}

	@Override
	public void setSourceId(String sourceSystemId) {
		this.sourceId = sourceSystemId;
	}
}
