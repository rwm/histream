/* Copyright (c) 2012, Raphael W. Majeed
 * All rights reserved. 
 * This file is subject to the conditions of the Simplified BSD License.
 * See LICENSE.txt or http://sourceforge.net/projects/histream/files/LICENSE.txt/view
 */
package histream.io;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractProcessedMessage implements ProcessedMessage {

	private List<MessageProcessingError> processingErrors;
	
	public AbstractProcessedMessage(){
		processingErrors = null;
	}
	
	@Override
	public abstract String getMessageID();
	
	@Override
	public boolean hasProcessingErrors() {
		return processingErrors != null;
	}

	@Override
	public Collection<MessageProcessingError> getProcessingErrors() {
		return processingErrors;
	}
	
	public void addProcessingError(MessageProcessingError error){
		if( processingErrors == null ){
			processingErrors = new LinkedList<MessageProcessingError>();
		}
		processingErrors.add(error);
	}

}
