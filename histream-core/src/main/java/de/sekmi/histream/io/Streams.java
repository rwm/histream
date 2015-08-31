package de.sekmi.histream.io;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationHandler;
import de.sekmi.histream.ObservationSupplier;
import de.sekmi.histream.impl.Meta;

public class Streams {
	/**
	 * TODO move method to ObservationSupplier
	 * @param supplier observation supplier
	 * @return spliterator
	 */
	public static Spliterator<Observation> nonNullSpliterator(Supplier<Observation> supplier){
		return new NonNullSpliterator(supplier);
	}
	/**
	 * Create a non-null stream of observations
	 * The stream will end when the first non null observation is 
	 * received via {@link Supplier#get()}, which usually means end of stream.
	 * 
	 * @param supplier observation supplier
	 * @return stream
	 */
	public static Stream<Observation> nonNullStream(Supplier<Observation> supplier){
		return StreamSupport.stream(new NonNullSpliterator(supplier), false);
	}
	
	/**
	 * Transfers meta information and all observations from source to target.
	 * 
	 * @param source observation source
	 * @param target observation handler
	 */
	public static void transfer(ObservationSupplier source, ObservationHandler target){
		Meta.transfer(source, target);
		Streams.nonNullStream(source).forEach(target);
	}
	
	private static class NonNullSpliterator implements Spliterator<Observation>{
		private Supplier<Observation> supplier;
		
		public NonNullSpliterator(Supplier<Observation> supplier) {
			this.supplier = supplier;
		}
		@Override
		public boolean tryAdvance(Consumer<? super Observation> action) {
			Observation o = supplier.get();
			if( o == null )return false;
			action.accept(o);
			return true;
		}

		@Override
		public Spliterator<Observation> trySplit() {
			return null;
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public int characteristics() {
			return Spliterator.NONNULL | Spliterator.IMMUTABLE;
		}
	}
}
