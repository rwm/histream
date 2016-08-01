package de.sekmi.histream.i2b2;

import de.sekmi.histream.Extension;
import de.sekmi.histream.Observation;

public class PatientNumExtension implements Extension<Integer>{

	@Override
	public Integer createInstance(Observation observation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer createInstance(Object... args) throws UnsupportedOperationException, IllegalArgumentException {
		if( args.length != 1 || args[0] == null || !(args[0] instanceof Integer) ){
			throw new IllegalArgumentException("Expecting single Integer argument");
		}
		return (Integer)args[0];
	}

	@Override
	public Class<?>[] getInstanceTypes() {
		return new Class<?>[]{Integer.class};
	}

}
