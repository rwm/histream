package de.sekmi.histream.io.transform;

import java.io.UncheckedIOException;
import java.util.function.Consumer;

import de.sekmi.histream.Observation;
import de.sekmi.histream.io.AbstractTransformer;

public class PushTransformer extends AbstractTransformer implements Consumer<Observation>{
	private Consumer<Observation> target;
	public PushTransformer(Consumer<Observation> target, Transformation transformation){
		super(transformation);
		this.target = target;
	}

	@Override
	public void accept(Observation t) {
		Observation ret;
		try {
			ret = transformation.transform(t, fifoPush);
		} catch (TransformationException e) {
			throw new UncheckedIOException(e);
		}
		
		if( ret != null ){
			target.accept(ret);
		}
		
		while( !fifo.isEmpty() ){
			target.accept( fifo.remove() );
		}
	}
}
