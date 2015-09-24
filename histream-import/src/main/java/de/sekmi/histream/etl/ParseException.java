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
	private String location;
	
	public ParseException(String message){
		super(message);
	}
	
	public ParseException(String message, Throwable cause){
		super(message, cause);
	}
	
	/**
	 * Set the location of the parse error.
	 * E.g. filename and line 
	 * @param location location string
	 */
	public void setLocation(String location){
		this.location = location;
	}
	
	/**
	 * Get the parse exception's location
	 * @return location string
	 */
	public String getLocation(){
		return location;
	}
	
	@Override
	public String toString(){
		if( location == null )return super.toString();
		else return super.toString() + ": "+location;
	}
}
