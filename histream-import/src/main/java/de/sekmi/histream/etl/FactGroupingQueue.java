package de.sekmi.histream.etl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import de.sekmi.histream.Observation;

/**
 * Algorithm:
 * Get first row from patient and visit.
 * Put first rows from each fact table in wait list
 * For each table, process all rows belonging to the current patient and visit
 * Try with next visit, repeat
 * Try with next patient, repeat
 * 
 * @author Raphael
 *
 */
public class FactGroupingQueue{
	private RecordSupplier<? extends FactRow> patientTable, visitTable;
	private List<RecordSupplier<? extends FactRow>> factTables;
	
	private FactRow currentPatient, nextVisit;
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


	public FactGroupingQueue(RecordSupplier<? extends FactRow> patientTable, RecordSupplier<? extends FactRow>visitTable){
		this.patientTable = patientTable;
		this.visitTable = visitTable;
		this.factTables = new ArrayList<>();
		this.workQueue = new ArrayDeque<>();
		
	}
	public void addFactTable(RecordSupplier<? extends FactRow> supplier){
		if( supplier == patientTable || supplier == visitTable )throw new IllegalArgumentException("Cannot add patient or visit table as fact table");
		if( factTables.contains(supplier) )throw new IllegalArgumentException("Supplier already added");
		factTables.add(supplier);
	}
	
	/**
	 * Load first row from each table, fill and sort the observation queue
	 */
	public void prepare(){
		currentRows = new ArrayList<>(factTables.size());
		// load first rows
		for( RecordSupplier<? extends FactRow> s : factTables ){
			FactRow r = s.get();
			currentRows.add(r);
		}
		tableIndex = 0;
		
		currentPatient = patientTable.get();
		// TODO sync patient with extension factory
		addFactsToWorkQueue(currentPatient);
		
		// for every patient, facts without visitId (=null) are parsed first
		currentVisitId = null;		
		nextVisit = visitTable.get();
		// TODO sync visit with extension factory
	}
	
	private void addFactsToWorkQueue(FactRow r){
		for( Observation f : r.getFacts() ){
			workQueue.add(f);
		}
	}
	
	public Observation next(){
		do{
			if( !workQueue.isEmpty() ){
				return workQueue.remove();
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
			// no more fitting facts in current prefetched rows
			// try to get next visit for current patient
			if( nextVisit != null && nextVisit.getPatientId().equals(currentPatient.getPatientId()) ){
				// next visit also belongs to current patient, continue
				currentVisitId = nextVisit.getVisitId();
				// TODO: sync visit with extension factory
				addFactsToWorkQueue(nextVisit);
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
					break;
				}
				// TODO: sync patient with extension factory
				addFactsToWorkQueue(currentPatient);
				tableIndex = 0;
				// goto top
				continue;
			}
		}while( factTables.size() > 0 || !workQueue.isEmpty() );
		return null; // done
	}
	
}
