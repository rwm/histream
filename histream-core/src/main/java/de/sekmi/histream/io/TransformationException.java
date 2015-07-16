package de.sekmi.histream.io;

import java.io.IOException;

public class TransformationException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public TransformationException(String message, Throwable cause){
		super(message,cause);
	}
	public TransformationException(String message){
		super(message);
	}
}
