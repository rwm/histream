package de.sekmi.histream;

/**
 * Exception which occurred in connection with an observation
 * 
 * @author Raphael
 *
 */
public class ObservationException extends Exception{
	private static final long serialVersionUID = 1L;
	
	private Observation fact;
	
	public ObservationException(Observation fact, Throwable cause){
		super(cause);
		this.fact = fact;
	}
	public ObservationException(String message, Observation fact, Throwable cause){
		super(message, cause);
		this.fact = fact;
	}
	
	public Observation getObservation(){return fact;}
}