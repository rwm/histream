package de.sekmi.histream.io;

import java.time.Instant;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;

public class AbstractObservationParser {
	protected ObservationFactory factory;
	// meta
	protected Instant sourceTimestamp;
	protected String sourceId;
	protected String etlStrategy;

	public AbstractObservationParser(){
	}
	
	public void setObservationFactory(ObservationFactory factory){
		this.factory = factory;
	}
	
	protected void parseSourceTimestamp(String sourceTimestamp){
		this.sourceTimestamp = javax.xml.bind.DatatypeConverter.parseDateTime(sourceTimestamp).toInstant();
	}
	protected void setSourceId(String sourceId){
		this.sourceId = sourceId;
	}
	
	protected void setEtlStrategy(String strategy){
		this.etlStrategy = strategy;
	}
	
	public static Spliterator<Observation> nonNullSpliterator(Supplier<Observation> supplier){
		return new NonNullSpliterator(supplier);
	}
	public static Stream<Observation> nonNullStream(Supplier<Observation> supplier){
		return StreamSupport.stream(new NonNullSpliterator(supplier), false);
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
