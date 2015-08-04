package de.sekmi.histream.etl;

public class ParseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParseException(String message){
		super(message);
	}
	
	public ParseException(String message, Throwable cause){
		super(message, cause);
	}
}