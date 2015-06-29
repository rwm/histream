package de.sekmi.histream.io;

import java.util.function.Consumer;

import de.sekmi.histream.Observation;

public class PushTransformer extends AbstractTransformer implements Consumer<Observation>{
	private Consumer<Observation> target;
	public PushTransformer(Consumer<Observation> target, Transformation transformation){
		super(transformation);
		this.target = target;
	}

	@Override
	public void accept(Observation t) {
		Observation ret = transformation.transform(t, fifoPush);
		
		if( ret != null ){
			target.accept(ret);			
		}
		
		while( !fifo.isEmpty() ){
			target.accept( fifo.remove() );
		}
	}
}
