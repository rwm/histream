package de.sekmi.histream.export;

import de.sekmi.histream.export.config.ExportException;

/**
 * Unchecked export exception which is used locally (in this package)
 * to pass export exception outside of stream operations.
 * 
 * @author R.W.Majeed
 *
 */
class UncheckedExportException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UncheckedExportException(ExportException cause){
		super(cause);
	}
	public ExportException getCause(){
		return (ExportException)super.getCause();
	}
}
