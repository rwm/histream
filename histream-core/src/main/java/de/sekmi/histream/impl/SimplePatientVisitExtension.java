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


import de.sekmi.histream.Extension;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

public class SimplePatientVisitExtension implements Extension<VisitPatientImpl>{
	private final static Class<?>[] TYPES = new Class[] {Visit.class,VisitPatientImpl.class,Patient.class,PatientImpl.class};

	@Override
	public Class<?>[] getInstanceTypes() {return TYPES;}

	@Override
	public VisitPatientImpl createInstance(Object... args) {
		if( args.length != 3 
				|| !(args[0] instanceof String)
				|| !(args[1] instanceof PatientImpl)
				|| !(args[2] instanceof ExternalSourceType) )
		{
			throw new IllegalArgumentException("Need arguments Patient id, Visit id, ExternalSourceType");
		}
		ExternalSourceType source = (ExternalSourceType)args[2];
		PatientImpl patient = new PatientImpl();
		patient.setId((String)args[0]);
		patient.setSourceId(source.getSourceId());
		patient.setSourceTimestamp(source.getSourceTimestamp());
		VisitPatientImpl visit = new VisitPatientImpl((String)args[1], patient, null);
		visit.setSourceId(source.getSourceId());
		visit.setSourceTimestamp(source.getSourceTimestamp());
		
		return visit;
	}

	@Override
	public VisitPatientImpl createInstance(Observation observation) {
		VisitPatientImpl visit = createInstance(observation.getPatientId(), observation.getEncounterId(), observation.getSource());
		//visit.setId();
		//visit.setPatientId(observation.getPatientId());
		//visit.setSourceId(observation.getSourceId());
		//visit.setSourceTimestamp(observation.getSourceTimestamp());
		return visit;
	}

	@Override
	public Class<VisitPatientImpl> getSlotType() {
		return VisitPatientImpl.class;
	}

	@Override
	public <U> U extractSubtype(VisitPatientImpl slotInstance, Class<U> subtype) {
		if( subtype.isAssignableFrom(PatientImpl.class) ){
			return subtype.cast(slotInstance.getPatient());
		}else if( subtype.isInstance(slotInstance) ) {
			return subtype.cast(slotInstance);
		}else {
			throw new IllegalArgumentException("Unsupported subtype "+subtype);
		}
	}

}
