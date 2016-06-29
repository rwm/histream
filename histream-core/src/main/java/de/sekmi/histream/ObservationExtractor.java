package de.sekmi.histream;

import java.time.Instant;

/**
 * Extracts observations from complex sources
 * such as databases and data warehouses
 * 
 * @author R.W.Majeed
 *
 */
public interface ObservationExtractor {

	ObservationSupplier extract(Instant start_min, Instant start_max, Iterable<String> notations) throws ObservationException;
}
