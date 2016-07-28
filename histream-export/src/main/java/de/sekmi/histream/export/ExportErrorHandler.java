package de.sekmi.histream.export;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import de.sekmi.histream.ObservationException;
import de.sekmi.histream.export.config.ExportException;

/**
 * Export error handler which unwraps {@link ExportException}s and {@link IOException}s
 * and rethrows them in unchecked exceptions {@link UncheckedExportException} and 
 * {@link UncheckedIOException}.
 * <p>
 * All other ObservationExceptions are forwarded to the parent error handler.
 * </p>
 * @author R.W.Majeed
 *
 */
class ExportErrorHandler implements Consumer<ObservationException>{

	private Consumer<ObservationException> parentHandler;

	public void setErrorHandler(Consumer<ObservationException> parentHandler){
		this.parentHandler = parentHandler;
	}
	@Override
	public void accept(ObservationException t) {
		Throwable cause = t.getCause();
		if( cause != null ){
			// unwrap export exception
			if( cause.getClass() == ExportException.class ){
				throw new UncheckedExportException((ExportException)cause);
			}else if( cause.getClass() == IOException.class ){
				throw new UncheckedIOException((IOException)cause);
			}
		}
		// pass to parent error handler
		if( parentHandler != null ){
			parentHandler.accept(t);
		}else{
			// or throw runtime exception if not specified
			throw new RuntimeException(t);
		}
	}

}
