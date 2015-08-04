package de.sekmi.histream.etl;

import java.util.List;

import de.sekmi.histream.Observation;

/**
 * Row containing parsed and processed facts from concept table 
 * 
 * @author Raphael
 *
 */
public interface FactRow {
	List<Observation> getFacts();
	String getPatientId();
	String getVisitId();
}
