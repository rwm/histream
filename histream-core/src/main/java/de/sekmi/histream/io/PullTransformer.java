package de.sekmi.histream.io;

import java.util.function.Supplier;

import de.sekmi.histream.Observation;

/**
 * Perform transformation of {@link Observation}s for a {@link Supplier}.
 * During the transformation, observations can be inserted, removed or modified.
 * <p>
 * Transformations are performed only on demand if {@link #get()} is called, 
 * until the supplier returns null.
 * @author Raphael
 *
 */
public class PullTransformer implements Supplier<Observation>{
	private Supplier<Observation> source;
	public PullTransformer(Supplier<Observation> source){
		this.source = source;
	}

	@Override
	public Observation get() {
		// TODO filter, buffer
		return source.get();
	}
}
