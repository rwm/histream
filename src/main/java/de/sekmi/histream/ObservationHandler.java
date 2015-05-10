package de.sekmi.histream;

import java.util.function.Consumer;

/**
 * Consumes observations. This interface can receive 
 * observations in any order.
 * <p>
 * Some observation handlers might want to assume certain 
 * grouping (e.g. patient/encounter grouping, for patient/encounter export)
 * or strict chronological order (e.g. for calculations, inference engines)
 * XXX this will be addressed in a future releases.
 * 
 * @author Raphael
 *
 */
public interface ObservationHandler extends Consumer<Observation>{
	/**
	 * Receive a single observation/fact.
	 * @param observation fact
	 */
	@Override
	void accept(Observation observation);
	
	void setErrorHandler(Consumer<ObservationException> handler);
}
