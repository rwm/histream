package de.sekmi.histream.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;


public class ProviderImpl<T> {
	private LinkedList<Consumer<T>> handlers;
	
	public ProviderImpl() {
		handlers = new LinkedList<>();
	}
	
	public void addHandler(Consumer<T> handler) {
		handlers.add(handler);
	}

	public void removeHandler(Consumer<T> handler) {
		handlers.remove(handler);
	}
	
	
	public void provide(T t){
		Iterator<Consumer<T>> iter = handlers.iterator();
		while( iter.hasNext() ){
			iter.next().accept(t);
		}
	}
}
