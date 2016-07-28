package de.sekmi.histream.export.config;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Iterate over objects which are contained in {@link Iterable}s each contained in other indirect
 * objects. Simply put, iterable over iterable, extracting the iterable from the indirect objects.
 * 
 * @author R.W.Majeed
 *
 * @param <T> desired type which will be should be iterated over
 * @param <U> indirect type which contains iterators to the desired type
 */
class IterableIterable<T,U> implements Iterable<T>{

	Function<U, Iterator<T>> extractor;
	Iterable<T> direct;
	Iterable<U> indirect;
	
	public IterableIterable(Iterable<U> indirect, Function<U,Iterator<T>> extractor, Iterable<T> direct){
		this.indirect = indirect;
		this.direct = direct;
		this.extractor = extractor;
	}
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Iterator<U> outer = indirect.iterator();
			Iterator<T> current = null;
			boolean last = false;

			@Override
			public boolean hasNext() {
				while( current == null || !current.hasNext() ){
					if( outer.hasNext() ){
						current = extractor.apply(outer.next());
					}else if( !last ){
						current = direct.iterator();
						last = true;
					}else{
						return false;
					}					
				}
				return true;
			}

			@Override
			public T next() {
				if( hasNext() ){
					return current.next();
				}else{
					throw new NoSuchElementException();
				}
			}
		};
	}
}