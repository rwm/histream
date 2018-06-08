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


import de.sekmi.histream.Observation;

import java.util.Arrays;

import de.sekmi.histream.Extension;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

public class SimpleVisitExtension implements Extension<VisitImpl>{
	private final static Iterable<Class<? super VisitImpl>> TYPES = Arrays.asList(Visit.class, VisitImpl.class);

	@Override
	public Iterable<Class<? super VisitImpl>> getInstanceTypes() {return TYPES;}

	@Override
	public VisitImpl createInstance(Object... args) {
		if( args.length != 3 
				|| !(args[0] instanceof String)
				|| !(args[1] instanceof Patient)
				|| !(args[2] instanceof ExternalSourceType) )
		{
			throw new IllegalArgumentException("Need arguments String, Patient, ExternalSourceType");
		}
		VisitImpl visit = new VisitImpl();
		visit.setId((String)args[0]);
		visit.setPatient(((Patient)args[1]));
		ExternalSourceType source = (ExternalSourceType)args[2];
		visit.setSourceId(source.getSourceId());
		visit.setSourceTimestamp(source.getSourceTimestamp());
		
		return visit;
	}

	@Override
	public VisitImpl createInstance(Observation observation) {
		VisitImpl visit = createInstance(observation.getEncounterId(), observation.getExtension(Patient.class), observation.getSource());
		//visit.setId();
		//visit.setPatientId(observation.getPatientId());
		//visit.setSourceId(observation.getSourceId());
		//visit.setSourceTimestamp(observation.getSourceTimestamp());
		return visit;
	}

}
