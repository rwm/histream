package de.sekmi.histream;

import java.io.IOException;
import java.time.Instant;

/**
 * Extracts observations from complex sources
 * such as databases and data warehouses.
 * 
 * @author R.W.Majeed
 *
 */
public interface ObservationExtractor {

	/**
	 * Extract observations with a start time stamp between the specified limits.
	 * Only observations with the specified notations are extracted.
	 * TODO evaluate change from ObservationException to IOException
	 * 
	 * @param start_min minimum time for observation start (inclusive)
	 * @param start_max maximum time for observation start (inclusive)
	 * @param notations concept notations. Specifies which observations are extracted.
	 * @return supplier for the extracted observations. Must be closed after use.
	 * @throws IOException error (e.g. database failure)
	 */
	ObservationSupplier extract(Instant start_min, Instant start_max, Iterable<String> notations) throws IOException;
}
