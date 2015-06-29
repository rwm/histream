package de.sekmi.histream.io;

import java.util.function.Consumer;

import de.sekmi.histream.Observation;

public class PushTransformer implements Consumer<Observation>{
	private Consumer<Observation> target;
	public PushTransformer(Consumer<Observation> target){
		this.target = target;
	}

	@Override
	public void accept(Observation t) {
		// TODO transform, buffer
		target.accept(t);
	}
}
