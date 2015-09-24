package histream.hl7filter.stats;

import java.util.Calendar;

public class MinuteStatistics extends FixedIntervalStatistics<MinuteStatistics.Record> {

	public MinuteStatistics(int numIntervals, long startingTimestamp) {
		super(60L*1000L, numIntervals, startingTimestamp);
	}

	public static class Record{
		int count;
		int size;
	}

	@Override
	protected Record emptyStatRecord() {
		return new Record();
	}

	@Override
	protected Record[] emptyStatRecords(int length) {
		return new Record[length];
	}
	
	
	/**
	 * Insert a new record to the statistics. One message is counted
	 * and size is incremented by the specified size.
	 *  
	 * @param now time when the message was received (usually System.getCurrentTimeMillis())
	 * @param size size of the received message
	 */
	public void insertOne(long now, int size){
		int i = indexForTimestamp(now);
		if( i >= 0 ){
			Record r = this.records[i];
			r.count ++;
			r.size += size;
		}
	}

	@Override
	protected long zeroIntervalTimestamp(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}

	@Override
	protected boolean needForRotateStats(long time) {
		return true;
	}
}
