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


import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationProvider;

public abstract class AbstractObservationProvider implements ObservationProvider {
	private Consumer<Observation> consumer;
	
	public AbstractObservationProvider() {
	}
	
	@Override
	public void setHandler(Consumer<Observation> consumer) {
		this.consumer = consumer;
	}
	
	
	public void provideObservation(Observation observation){
		/*Iterator<ObservationHandler> iter = handlers.iterator();
		while( iter.hasNext() ){
			iter.next().accept(observation);
		}*/
		consumer.accept(observation);
	}

}
