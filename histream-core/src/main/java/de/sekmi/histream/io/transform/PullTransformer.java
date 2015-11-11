package de.sekmi.histream.io.transform;

import java.io.UncheckedIOException;
import java.util.function.Supplier;

import de.sekmi.histream.Observation;
import de.sekmi.histream.io.AbstractTransformer;

/**
 * Perform transformation of {@link Observation}s for a {@link Supplier}.
 * During the transformation, observations can be inserted, removed or modified.
 * <p>
 * Transformations are performed only on demand if {@link #get()} is called, 
 * until the supplier returns null.
 * @author Raphael
 *
 */
public class PullTransformer extends AbstractTransformer implements Supplier<Observation>{
	final private Supplier<Observation> source;
	
	public PullTransformer(Supplier<Observation> source, Transformation transformation){
		super(transformation);
		this.source = source;
	}

	/**
	 * Execute this transformation after another transformation
	 * @param nextTransformation previous transformation
	 * @return composite transformer which executes this transformation after the specified previous transformation
	 */
	public PullTransformer andThen(Transformation nextTransformation){
		return new PullTransformer(this, nextTransformation);
	}
	@Override
	public Observation get() {
		Observation ret;
		do{
			if( !fifo.isEmpty() ){ // try to empty queue
				ret = fifo.remove();
				break;
			}
			
			// next transformation
			Observation o = source.get();
			if( o == null ){
				// source depleted
				ret = null;
				break;
			}
		
			try {
				ret = transformation.transform(o, fifoPush);
			} catch (TransformationException e) {
				throw new UncheckedIOException(e);
			}
		}while( ret == null );
		
		return ret;
	}
}
