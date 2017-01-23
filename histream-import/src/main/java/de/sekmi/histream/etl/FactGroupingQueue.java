package de.sekmi.histream.etl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Supplier;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;

/**
 * Algorithm:
 * <ul>
 *  <li>Get first row from patient and visit.</li>
 *  <li>Put first rows from each fact table in wait list</li>
 *  <li>For each table, process all rows belonging to the current patient and visit</li>
 *  <li>Try with next visit, repeat</li>
 *  <li>Try with next patient, repeat</li>
 * </ul>
 * @author R.W.Majeed
 *
 */
public class FactGroupingQueue implements Supplier<Observation>{
	private RecordSupplier<PatientRow> patientTable;
	private RecordSupplier<VisitRow> visitTable;
	private PatientLookup patientLookup;
	private VisitLookup visitLookup;
	
	private List<RecordSupplier<? extends FactRow>> factTables;


	private PatientRow currentPatient;
	private VisitRow nextVisit;
	
	private Patient currentPatientInstance;
	private Visit currentVisitInstance;

	private String currentVisitId;
	private List<FactRow> currentRows;
	private Queue<Observation> workQueue;
	/**
	 * Table index in factTables which was last loaded to workQueue
	 */
	private int tableIndex;

	
	/**
	 * String comparison with nulls coming first
	 * @param v1 fist string
	 * @param v2 second string
	 * @return comparison result
	 */
	private static int compareWithNulls(String v1, String v2){
		int c;
		if( v1 == null && v2 == null )c = 0;
		else if( v1 == null )c = -1;
		else if( v2 == null )c = 1;
		else c = v1.compareTo(v2);
		return c;
	}


	public FactGroupingQueue(){
		this.factTables = new ArrayList<>();
		this.workQueue = new ArrayDeque<>();
	}
	public void setPatientTable(RecordSupplier<PatientRow> patientTable){
		this.patientTable = patientTable;
	}
	public void setVisitTable(RecordSupplier<VisitRow> visitTable){
		this.visitTable = visitTable;		
	}
	public void addFactTable(RecordSupplier<? extends FactRow> supplier){
		if( supplier == patientTable || supplier == visitTable )throw new IllegalArgumentException("Cannot add patient or visit table as fact table");
		if( factTables.contains(supplier) )throw new IllegalArgumentException("Supplier already added");
		factTables.add(supplier);
	}

	/**
	 * Set a patient lookup provider. The lookup is performed for
	 * each parsed patient before any visit or facts are parsed.
	 * <p>
	 * This can be used to synchronize the parsed patient with
	 * external storage</p>
	 * 
	 * @param lookup patient lookup
	 */
	public void setPatientLookup(PatientLookup lookup){
		this.patientLookup = lookup;
	}
	
	/**
	 * Set a visit lookup provider. The lookup is performed for
	 * each parsed visit before any facts are parsed.
	 * <p>
	 * This can be used to synchronize the parsed visit data with
	 * external storage</p>
	 * 
	 * @param lookup visit lookup
	 */
	public void setVisitLookup(VisitLookup lookup){
		this.visitLookup = lookup;
	}
	
	/**
	 * Current patient changed: {@link #currentPatient}
	 */
	private void patientChanged(){
		if( patientLookup != null ){
			currentPatientInstance = patientLookup.lookupPatient(currentPatient, patientTable.getSource());
		}else{
			// no lookup, just use the data we have
			currentPatientInstance = currentPatient;
		}
		addFactsToWorkQueue(currentPatient);
	}

	/**
	 * Current visit changed. Current visit id is in {@link #currentVisitId}.
	 * <p>
	 *  For facts without visit information, {@link #currentVisitId} may be null.
	 *  This will be the case once for every patient.
	 *  If {@link #currentVisitId} is not null, nextVisit will contain the 
	 *  current visit's information.
	 * </p>
	 */
	private void visitChanged(){

		if( currentVisitId == null ){
			// set visit extension to null
			currentVisitInstance = null;
			// TODO later support facts without encounter
		}else{
			// sync visit with extension factory
			if( visitLookup != null ){
				// currentVisitId? == nextVisit.getId()??
				currentVisitInstance = visitLookup.lookupVisit(currentPatientInstance, nextVisit, visitTable.getSource());				
			}else{
				currentVisitInstance = nextVisit;
			}
			
			addFactsToWorkQueue(nextVisit);
		}
	}

	/**
	 * Load first row from each table, fill and sort the observation queue
	 */
	public void prepare(){
		Objects.requireNonNull(patientTable);
		Objects.requireNonNull(visitTable);
		currentRows = new ArrayList<>(factTables.size());
		// load first rows
		for( RecordSupplier<? extends FactRow> s : factTables ){
			FactRow r = s.get();
			currentRows.add(r);
		}
		tableIndex = 0;
		
		currentPatient = patientTable.get();
		if( currentPatient != null ){
			// at least one patient row available
			// prepare the queue
			patientChanged();
			
			// for every patient, facts without visitId (=null) are parsed first
			currentVisitId = null;		
			visitChanged(); // maybe not needed, visit is already null
			
			nextVisit = visitTable.get();

		}else{ // currentPatient == null
			// patient table empty
			// unable to provide any data
			currentVisitId = null;
			nextVisit = null;
			// without patients, there can be no facts
			factTables.clear(); // remove fact tables
		}
	}
	
	private void addFactsToWorkQueue(FactRow r){
		for( Observation f : r.getFacts() ){
			// set patient extension
			patientLookup.assignPatient(f, currentPatientInstance);
			visitLookup.assignVisit(f, currentVisitInstance);
			// use visit start date if there is no start date in the observation
			if( f.getStartTime() == null && currentVisitInstance != null ){
				f.setStartTime(currentVisitInstance.getStartTime());
			}
			// TODO throw error/warning for facts without start date???

			workQueue.add(f);
		}
	}
	
	/**
	 * Called after all facts for the current visit are processed.
	 * <p>
	 *  This will also be the case for the {@code null}-Visit which
	 *  occurs once for each patient with the purpose to allow non-visit
	 *  related facts to be included. In this case, {@link #getVisit()}
	 *  will return {@code null}.
	 * </p>
	 */
	protected void visitFinished(){
		// nothing to do
	}
	/**
	 * Get the current patient information. This will be a patient
	 * object returned by {@link PatientLookup}.
	 * <p>
	 * This method is useful for subclasses which override {@link #visitFinished()}.
	 * </p>
	 * @return current patient.
	 */
	protected Patient getPatient(){
		return currentPatientInstance;
	}
	/**
	 * Get the current visit information. This will be a visit
	 * object returned by {@link VisitLookup}.
	 * <p>
	 * This method is useful for subclasses which override {@link #visitFinished()}.
	 * </p>
	 * @return current visit.
	 */
	protected Visit getVisit(){
		return currentVisitInstance;
	}
	
	@Override
	public Observation get(){
		do{
			if( !workQueue.isEmpty() ){
				return workQueue.remove();
			}

			if( currentPatient == null && currentVisitId == null ){
				// no more facts
				break;
			}
			// queue is empty, try to find table with matching facts
			while( tableIndex < factTables.size() ){
				FactRow row = currentRows.get(tableIndex);
				if( row == null ){
					// table empty, remove tables from list
					currentRows.remove(tableIndex);
					factTables.remove(tableIndex);
					continue; // index will now point to next table
				}else if( row.getPatientId().equals(currentPatient.getPatientId()) && compareWithNulls(row.getVisitId(), currentVisitId) == 0 ){
					// row fits into current group
					addFactsToWorkQueue(row);
					// prefetch next row
					currentRows.set(tableIndex, factTables.get(tableIndex).get());
					if( workQueue.isEmpty() ){
						// no facts found
						// can only happen if a fact table row contains no facts
						// which can occur if some filters prevent the facts from being generated
						// try to fetch next row
						continue;
					}
					return workQueue.remove();
				}else{
					// no fitting facts in table index, try next index
					tableIndex ++;
				}
			}
			visitFinished();
			// no more fitting facts in current prefetched rows
			// try to get next visit for current patient
			if( nextVisit != null && nextVisit.getPatientId().equals(currentPatient.getPatientId()) ){
				// next visit also belongs to current patient, 
				// continue with same patient id (but next visit)
				currentVisitId = nextVisit.getVisitId();
				visitChanged();
				
				// prefetch next visit
				nextVisit = visitTable.get();
				
				tableIndex = 0;
				// goto top
				continue;
			}else{
				// next visit belongs to other patient (or no more visits), try to load next patient
				currentVisitId = null;
				currentPatient = patientTable.get();
				if( currentPatient == null ){
					// no more patients available and work queue empty
					// we are done 
					// (during next call, currentVisitId and currentPatientId are null)
					break;
				}
				patientChanged();
				visitChanged();
				
				tableIndex = 0;
				// goto top
				continue;
			}
		}while( factTables.size() > 0 || !workQueue.isEmpty() || nextVisit != null );
		return null; // done
	}
	
}
