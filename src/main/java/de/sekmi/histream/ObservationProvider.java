package de.sekmi.histream;

import java.util.function.Consumer;

public interface ObservationProvider{
	void setHandler(Consumer<Observation> consumer);
	
	Class<?>[] getSupportedExtensions();
}
