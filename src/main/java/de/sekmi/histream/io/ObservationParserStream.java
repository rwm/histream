package de.sekmi.histream.io;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import de.sekmi.histream.Observation;

public class ObservationParserStream implements Stream<Observation>{

	@Override
	public Iterator<Observation> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Spliterator<Observation> spliterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isParallel() {
		return false;
	}

	@Override
	public Stream<Observation> sequential() {
		return this;
	}

	@Override
	public Stream<Observation> parallel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Observation> unordered() {
		return this;
	}

	@Override
	public Stream<Observation> onClose(Runnable closeHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Stream<Observation> filter(Predicate<? super Observation> predicate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> Stream<R> map(Function<? super Observation, ? extends R> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super Observation> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super Observation> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super Observation> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> Stream<R> flatMap(
			Function<? super Observation, ? extends Stream<? extends R>> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream flatMapToInt(
			Function<? super Observation, ? extends IntStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongStream flatMapToLong(
			Function<? super Observation, ? extends LongStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream flatMapToDouble(
			Function<? super Observation, ? extends DoubleStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Observation> distinct() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Observation> sorted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Observation> sorted(Comparator<? super Observation> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Observation> peek(Consumer<? super Observation> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Observation> limit(long maxSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Observation> skip(long n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forEach(Consumer<? super Observation> action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forEachOrdered(Consumer<? super Observation> action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observation reduce(Observation identity,
			BinaryOperator<Observation> accumulator) {
		Observation result = identity;
		/*for (T element : this stream)
			result = accumulator.apply(result, element) */
		return result;
	}

	@Override
	public Optional<Observation> reduce(BinaryOperator<Observation> accumulator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U reduce(U identity,
			BiFunction<U, ? super Observation, U> accumulator,
			BinaryOperator<U> combiner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> R collect(Supplier<R> supplier,
			BiConsumer<R, ? super Observation> accumulator,
			BiConsumer<R, R> combiner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R, A> R collect(Collector<? super Observation, A, R> collector) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Observation> min(Comparator<? super Observation> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Observation> max(Comparator<? super Observation> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count() {
		return mapToLong(e -> 1L).sum();
	}

	@Override
	public boolean anyMatch(Predicate<? super Observation> predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allMatch(Predicate<? super Observation> predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean noneMatch(Predicate<? super Observation> predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<Observation> findFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Observation> findAny() {
		// TODO Auto-generated method stub
		return null;
	}

}
