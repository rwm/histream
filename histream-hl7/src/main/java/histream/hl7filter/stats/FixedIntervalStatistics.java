package histream.hl7filter.stats;

// TODO: add function to determine wheter the statistics should be rotated

import java.util.logging.Logger;

public abstract class FixedIntervalStatistics<T> {
	static final Logger log = Logger.getLogger(FixedIntervalStatistics.class.getName());
	/**
	 * Maximum number of minutes to store for high resolution statistics.
	 */
	private int maxIntervals;
	
	/**
	 * Number of minutes which are cut off from the bottom of the 
	 * statistics and appended to the front once the maximum number
	 * of minutes are reached
	 */
	private int intervalsToRotate;
	T[] records;
	private long startingIntervalMillis;
	private long intervalInMillis;
	private RemovedRecordHandler<T> handler;
	

	/**
	 * Interface to process records when they are removed
	 * from the bottom of the statistics once new space
	 * is needed.
	 * 
	 * @author marap1
	 *
	 */
	public interface RemovedRecordHandler<T>{
		void statRecordRemoved(T record);
	}


	protected abstract T emptyStatRecord();

	protected abstract T[] emptyStatRecords(int length);

	/**
	 * Called only if a provided time is in the future of
	 * the statistics interval. 
	 * 
	 * @param time
	 * @return Whether to rotate the statistics. If not, index -1 is returned.
	 */
	protected abstract boolean needForRotateStats(long time);
	
	public FixedIntervalStatistics(long intervalInMillis, int numIntervals, long startingTimestamp, int intervalsToRotate){
		this.intervalInMillis = intervalInMillis;
		initRecords(numIntervals);
		
		this.startingIntervalMillis = zeroIntervalTimestamp(startingTimestamp);
		this.intervalsToRotate = intervalsToRotate;
	}
	public FixedIntervalStatistics(long intervalInMillis, int numIntervals, long startingTimestamp){
		/* intervals to rotate: 30% */
		this(intervalInMillis,numIntervals,startingTimestamp,(int)(numIntervals * 0.3f));
	}
	
	protected abstract long zeroIntervalTimestamp(long timestamp);
	
	/**
	 * Creates a new HighResStats object which is able to
	 * calculates statistics starting with the current minute.
	 * 
	 * @param hours number of hours to keep high res statistics.
	 */
	/*
	 public FixedIntervalStatistics(long intervalInMillis, int numIntervals){
	 
		this.intervalInMillis = intervalInMillis;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0); // TODO: change time to match intervall for zero
		initRecords(numIntervals);
		this.startingIntervalMillis = cal.getTimeInMillis();
		
	}
	*/

	public void setRemovedRecordHandler(RemovedRecordHandler<T> removedHandler){
		this.handler = removedHandler;
	}
	public long getStartingTime(){return startingIntervalMillis;}
	
	public int getNumIntervals(){return maxIntervals;}
	public long getIntervalInMillis(){return intervalInMillis;}
	
	private void initRecords(int numIntervals){
		this.maxIntervals = numIntervals;
		records = emptyStatRecords(numIntervals);
		for( int i=0; i<records.length; i++ ){
			records[i] = emptyStatRecord();
		}
	}
	
	
	
	
	/**
	 * Make new space by removing the first 'intervalsToRotate' from
	 * the statistics and append them to the front.
	 */
	private void rotateStats(){
		T[] newRecords = emptyStatRecords(maxIntervals);
		/* copy old records */
		for( int i=intervalsToRotate; i<records.length; i++ ){
			newRecords[i-intervalsToRotate] = records[i];
		}
		for( int i=newRecords.length-intervalsToRotate; i<newRecords.length; i++ ){
			newRecords[i] = emptyStatRecord();
		}
		/* replace array */
		T[] oldRecords = this.records;
		this.records = newRecords;
		
		/* adjust starting time */
		this.startingIntervalMillis += intervalsToRotate*intervalInMillis;
		
		/* process removed records */
		if( handler != null ){
			for( int i=0; i<intervalsToRotate; i++ ){
				handler.statRecordRemoved(oldRecords[i]);
			}
		}
		
	}
	
	/**
	 * Determine the statistics slot index for the given time.
	 * If the time is outside in the past, -1 is returned.
	 * If the time is outside in the future (and no rotation occurs), -2 is returned.
	 * @param unixTime
	 * @return a valid index >= 0, or -1 for past or -2 for future.
	 */
	protected int indexForTimestamp(long unixTime){
		long delta = unixTime - startingIntervalMillis;
		
		delta /= intervalInMillis; // convert to minutes

		if( delta >= maxIntervals ){
			// outside of time range.
			
			
			// determine whether to rotate statistics: 
			//  cut off from the bottom and append to front.
			if( needForRotateStats(unixTime) ){
				log.finest("Rotating statistics ("+intervalsToRotate+" of "+maxIntervals+" intervals)");
				rotateStats();

				/* recurse again, should return valid index this time */
				return indexForTimestamp(unixTime);
			
			}else{
				// don't rotate, return outside of range (future)
				return -2;
			}
			
		}else if( delta < 0 ){
			/* time outside of the statistics time frame (past) */
			return -1;
		}else{
			return (int)delta;
		}
	}
	

}
