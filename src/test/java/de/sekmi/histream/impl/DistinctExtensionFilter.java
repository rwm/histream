package de.sekmi.histream.impl;

import java.util.function.Consumer;

import de.sekmi.histream.Observation;

/**
 * Calls a distinct extension consumer for each distinct extension found in successive observations
 * @author Raphael
 *
 * @param <T>
 */
public class DistinctExtensionFilter<T> implements Consumer<Observation>{
	private Class<T> distinctClass;
	private Object prev;
	private Consumer<T> distinctConsumer;
	private Consumer<Observation> factConsumer;
	
	
	public DistinctExtensionFilter(Consumer<Observation> factConsumer, Class<T> visitClass, Consumer<T> distinctConsumer){
		this.distinctClass = visitClass;
		this.factConsumer = factConsumer;
		this.distinctConsumer = distinctConsumer;
	}
	@Override
	public void accept(Observation t) {
		T cur = t.getExtension(distinctClass);
		if( cur != prev ){
			distinctConsumer.accept(cur);
			prev = cur;
		}
		factConsumer.accept(t);
	}

}
