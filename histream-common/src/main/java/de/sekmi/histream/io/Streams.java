package de.sekmi.histream.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
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
	 * Transfers all observations from source to target.
	 * Does not transfer any meta information.
	 * <p>
	 * For transferring meta information, 
	 * see {@link Meta#transfer(ObservationSupplier, ObservationHandler)}
	 * 
	 * @param source observation source
	 * @param target observation handler
	 */
	public static void transfer(Supplier<Observation> source, Consumer<Observation> target){
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
	
	public static long channelCopy(final ReadableByteChannel src, final WritableByteChannel dest)
			throws IOException {
		long count = 0;
		final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
		while (src.read(buffer) != -1) {
			// prepare the buffer to be drained
			buffer.flip();
			// write to the channel, may block
			count += dest.write(buffer);
			// If partial transfer, shift remainder down
			// If buffer is empty, same as doing clear()
			buffer.compact();
		}
		// EOF will leave buffer in fill state
		buffer.flip();
		// make sure the buffer is fully drained.
		while (buffer.hasRemaining()) {
			count += dest.write(buffer);
		}
		return count;
	}
	
	public static long streamCopy(InputStream in, OutputStream out) throws IOException{
		byte[] buffer = new byte[1024]; // Adjust if you want
		long total = 0;
		int bytesRead;
		while ((bytesRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
			total += bytesRead;
		}
		return total;
	}

}
