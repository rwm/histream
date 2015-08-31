package de.sekmi.histream.etl;

/**
 * Error condition during parsing of input tables. 
 * 
 * E.g. when a column header reference in the configuration
 * does not appear in the respective table.
 * 
 * @author Raphael
 *
 */
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
