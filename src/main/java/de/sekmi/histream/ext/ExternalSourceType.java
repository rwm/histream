package de.sekmi.histream.ext;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
	 * @param sourceTimestamp source timestamp
	 */
	void setSourceTimestamp(Instant sourceTimestamp);
	/**
	 * Get id or name of the source system.
	 * @return source id 
	 */
	String getSourceId();
	void setSourceId(String sourceSystemId);
	
}
