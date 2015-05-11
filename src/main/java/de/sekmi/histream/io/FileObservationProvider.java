package de.sekmi.histream.io;

import java.util.function.Supplier;

import de.sekmi.histream.Observation;

/**
 * Converts a file into a supply of observations.
 * <p>
 * When an instance is constructed, meta information should be read from
 * the file (e.g. etl strategy and other instructions)
 * <p>
 * TODO shouldn't this interface extend Closable?
 * TODO maybe add error handler
 * @author Raphael
 *
 */
public interface FileObservationProvider extends Supplier<Observation>{
	
	/**
	 * Retrieve meta information for this supply of observations.
	 * <p>
	 * Possible keys are source.id, source.timestamp, etl.strategy
	 * @param key meta key
	 * @return value for the meta key
	 */
	String getMeta(String key);
}
