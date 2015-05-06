package de.sekmi.histream.impl;

import de.sekmi.histream.Observation;
import de.sekmi.histream.Extension;
import de.sekmi.histream.ext.Visit;

public class SimpleVisitExtension implements Extension<VisitImpl>{
	private final static Class<?>[] TYPES = new Class<?>[]{Visit.class, VisitImpl.class};

	@Override
	public Class<?>[] getInstanceTypes() {return TYPES;}

	@Override
	public VisitImpl createInstance() {
		return new VisitImpl();
	}

	@Override
	public VisitImpl createInstance(Observation observation) {
		VisitImpl visit = createInstance();
		visit.setId(observation.getEncounterId());
		return visit;
	}

}
