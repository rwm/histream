package de.sekmi.histream.impl;

public interface FlatObservationWriter {

	void writeMeta(String meta, String value);
	void writeConceptMap(String concept, String map);
	void beginGroup();
	void endGroup();
	void writeObservation(String[] fields);
}
