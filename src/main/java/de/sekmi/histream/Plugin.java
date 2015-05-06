package de.sekmi.histream;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * <p>
 * A Plugin should implement a constructor accepting a single 
 * {@link Properties} object for configuration. The {@link Closeable#close()}
 * method is called when the plugin is unloaded or during normal termination.
 * 
 * @author Raphael
 *
 */
public interface Plugin extends Closeable{


	@Override
	default void close()throws IOException{}
}
