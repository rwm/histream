package de.sekmi.histream.etl;

import java.util.List;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.VisitPatientImpl;

/**
 * Row containing parsed and processed facts from concept table 
 * 
 * @author Raphael
 *
 */
public interface FactRow {
	/**
	 * Get a list of facts for this row.
	 * The list will be populated by a previous call to 
	 * {@link #createFacts(PatientImpl, VisitPatientImpl)}.
	 * This method always return the same list so the list's
	 * items can be modified.
	 * @return list of facts for this row 
	 */
	List<Observation> getFacts();
	/**
	 * Get the patient id for this row. Can be called before 
	 * {@link #createFacts(PatientImpl, VisitPatientImpl)} 
	 * or {@link #getFacts()}.
	 * @returns patient id
	 */
	String getPatientId();
	/**
	 * Get the visit id for this row. See {@link #getPatientId()}.
	 * @return visit id
	 */
	String getVisitId();

	/**
	 * Create facts for this row. This method must be called before {@link #getFacts()}, otherwise {@link #getFacts()} will fail.
	 * @param patient patient context for this row
	 * @param visit visit context for this row
	 * @param factory observation factory
	 */
	void createFacts(PatientImpl patient, VisitPatientImpl visit, ObservationFactory factory);
}
