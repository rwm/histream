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


import de.sekmi.histream.Extension;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;

/**
 * Basic implementation of patient extension which 
 * always creates new instances of {@link PatientImpl}.
 * 
 * @author R.W.Majeed
 *
 */
public class SimplePatientExtension implements Extension<PatientImpl>{
	private final static Class<?>[] TYPES = new Class<?>[]{Patient.class, PatientImpl.class};

	@Override
	public Class<?>[] getInstanceTypes() {return TYPES;}

	@Override
	public PatientImpl createInstance(Object... args) {
		if( args.length != 2 || !(args[0] instanceof String) || !(args[1] instanceof ExternalSourceType) ){
			throw new IllegalArgumentException("need String patid, ExternalSourceType source");
		}
		PatientImpl p = new PatientImpl();

		p.setId((String)args[0]);
		ExternalSourceType s = (ExternalSourceType)args[1];

		p.setSourceId(s.getSourceId());
		p.setSourceTimestamp(s.getSourceTimestamp());

		return p;
	}

	@Override
	public PatientImpl createInstance(Observation observation) {
		PatientImpl patient = createInstance(observation.getPatientId(), observation.getSource());
		return patient;
	}

}
