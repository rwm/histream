package de.sekmi.histream.ext;

import java.time.Instant;

/**
 * Extension type, which comes from an external source
 * e.g. other information system or database
 * @author Raphael
 *
 */
public interface ExternalSourceType {

	/** 
	 * Get the point in time, when exactly the contained information left it's source.
	 * @return source timestamp
	 */
	Instant getSourceTimestamp();
	
	/**
	 * Set the point in time, when the contained information left it's source.
	 * @param instant
	 */
	void setSourceTimestamp(Instant sourceTimestamp);
	/**
	 * Get id or name of the source system.
	 * @return source id 
	 */
	String getSourceId();
	void setSourceId(String sourceSystemId);
	
}
