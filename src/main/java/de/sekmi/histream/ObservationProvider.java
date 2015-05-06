package de.sekmi.histream;

public interface ObservationProvider{
	void addHandler(ObservationHandler handler);
	void removeHandler(ObservationHandler handler);
	
	Class<?>[] getSupportedExtensions();
}
