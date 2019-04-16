package de.sekmi.histream.impl;

import java.util.Hashtable;
import java.util.Map;

/**
 * Patient extension which caches patients in a {@link Hashtable}.
 * 
 * @author R.W.Majeed
 *
 */
@Deprecated
public class CachedPatientExtension extends SimplePatientExtension {
	private Map<String, PatientImpl> cache;

	public CachedPatientExtension() {
		cache = new Hashtable<>();
	}
	@Override
	public PatientImpl createInstance(Object... args) {
		if( args.length < 1 || !(args[0] instanceof String) ){
			throw new IllegalArgumentException("First argument should be String patient id");
		}
		String pid = (String)args[0];
		
		PatientImpl cached = cache.get(pid);
		if( cached == null ){
			// not found in cache
			// create
			cached = super.createInstance(args);
			// put in cache
			cache.put(pid, cached);
		}
		return cached;
	}
}
