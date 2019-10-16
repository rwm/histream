package de.sekmi.histream.etl.config;

import java.util.ArrayList;
import java.util.List;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.PatientImpl;
import de.sekmi.histream.impl.VisitPatientImpl;

/**
 * Wide fact row which. A single wide row
 * may contain multiple facts.
 * 
 * @author R.W.Majeed
 *
 */
public class WideRow extends AbstractFactRow{
	private WideTable table;
	
	public WideRow(WideTable table, String patientId, String visitId){
		super(patientId,visitId);
		this.table = table;
	}


}
