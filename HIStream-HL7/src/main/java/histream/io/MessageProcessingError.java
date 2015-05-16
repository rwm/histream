/* Copyright (c) 2012, Raphael W. Majeed
 * All rights reserved. 
 * This file is subject to the conditions of the Simplified BSD License.
 * See LICENSE.txt or http://sourceforge.net/projects/histream/files/LICENSE.txt/view
 */
package histream.io;

public class MessageProcessingError {

	protected Throwable cause;
	protected String message;
	
	public MessageProcessingError(Throwable cause){
		this.cause = cause;
	}
	public MessageProcessingError(String message, Throwable cause){
		this(cause);
		this.message = message;
	}
}
