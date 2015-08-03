package de.sekmi.histream.etl;

import java.util.Spliterator;
import java.util.function.Consumer;

public class VisitSupplier implements Spliterator<VisitRow>{

	@Override
	public boolean tryAdvance(Consumer<? super VisitRow> action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Spliterator<VisitRow> trySplit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long estimateSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int characteristics() {
		// TODO Auto-generated method stub
		return 0;
	}

}
