package de.sekmi.histream.etl;

public class UncheckedParseException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UncheckedParseException(ParseException cause){
		super(cause);
	}
	
	@Override
	public ParseException getCause(){
		return (ParseException)super.getCause();
	}
	
}
