package de.sekmi.histream.inference;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DependencyExpandingIterator implements Iterator<String> {
	private InferenceFactory factory;
	private Iterator<String> concepts;
	/** Temporary iterator for dependencies */
	private Iterator<String> temp;
	private String next;
	
	public DependencyExpandingIterator(InferenceFactory factory, Iterator<String> concepts) {
		this.concepts = concepts;
		this.factory = factory;
	}
	
	@Override
	public boolean hasNext() {
		prefetch();
		return null != next;
	}

	private void prefetch(){
		while( next == null ){
			if( temp != null && temp.hasNext() ){
				// fetch next dependency
				next = temp.next();
			}else if( concepts.hasNext() ){
				next = concepts.next();
				// try whether this concept is inferred
				InferredConcept ic = factory.getConceptById(next);
				if( ic == null ){
					// can not infer this concept, leave as is
				}else{
					// can infer this concept
					// remove the concept id and replace with dependency IDs
					next = null;
					temp = ic.getDependencyIDs();
					// continue, fetch first dependency in next iteration
				}
			}else{
				// no more concepts
				break;
			}
		}
	}
	@Override
	public String next() {
		prefetch();
		if( next != null ){
			throw new NoSuchElementException();
		}
		return next;
	}

}
