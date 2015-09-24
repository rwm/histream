package histream.hl7filter.stats;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

public class HourlyStatistics extends FixedIntervalStatistics<HourlyStatistics.Record> {

	Hashtable<String, SizeAndCount> messageVersions;
	Record farFuture;
	Record farPast;
	long rotateSysdate;
	
	public HourlyStatistics(int numIntervals, long minimumTime, int rotateAfterDays) {
		super(1000L*60L*60L, numIntervals, minimumTime, rotateAfterDays);
		messageVersions = new Hashtable<String, SizeAndCount>();
		farFuture = new Record();
		farPast = new Record();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, rotateAfterDays);

		this.rotateSysdate = cal.getTimeInMillis();
	}

	public static class SubRecord implements SizeAndCount{
		int size;
		int count;
		@Override
		public long getCount() {return count;}
		@Override
		public long getSize() {return size;}
		@Override
		public void incrementCount() {count ++;}
		@Override
		public void addSize(int size) {this.size += size;}
	}
	
	public static class LongRecord implements SizeAndCount{
		long size;
		int count;
		@Override
		public long getCount() {return count;}
		@Override
		public long getSize() {return size;}
		@Override
		public void incrementCount() {count ++;}
		@Override
		public void addSize(int size) {this.size += size;}
	}
	public static class Record implements SizeAndCount{
		private int size;
		private int count;
		Hashtable<String, SizeAndCount> senders;
		Hashtable<String, SizeAndCount> events;
		
		public Record(){
			senders = new Hashtable<String, SizeAndCount>();
			events = new Hashtable<String, SizeAndCount>();
		}
		private static void incrementSubrecord(Hashtable<String, SizeAndCount> ht, String key, int size){
			SizeAndCount s = ht.get(key);
			if( s == null ){
				s = new SubRecord();
				ht.put(key, s);
			}
			s.incrementCount();
			s.addSize(size);
		}
		public void incrementSender(String sender, int size){
			incrementSubrecord(senders, sender, size);
		}
		public void incrementEvent(String event, int size){
			incrementSubrecord(events, event, size);
		}
		@Override
		public long getCount() {return this.count;}
		@Override
		public long getSize() {return this.size;}
		@Override
		public void incrementCount() {this.count ++;}
		@Override
		public void addSize(int size) {this.size += size;}
		
	}

	@Override
	protected Record emptyStatRecord() {
		return new Record();
	}

	@Override
	protected Record[] emptyStatRecords(int length) {
		return new Record[length];
	}
	
	public String messageSizesByTypes(int i){
		return hashtableCounts(records[i].events);
	}
	public String messageSizesBySenders(int i){
		return hashtableCounts(records[i].senders);
	}
	
	public static String hashtableCounts(Hashtable<String,SizeAndCount> ht){
		StringBuilder b = new StringBuilder();
		Enumeration<String> keys = ht.keys();
		while( keys.hasMoreElements() ){
			String key = keys.nextElement();
			SizeAndCount sr = ht.get(key);
			b.append(key).append("=").append(sr.getCount()).append(' ');
		}
		return b.toString();
	}
	
	/**
	 * Insert a new record to the statistics. One message is counted
	 * and size is incremented by the specified size.
	 *  
	 * @param now time when the message was received (usually System.getCurrentTimeMillis())
	 * @param size size of the received message
	 */
	public void insertOne(long now, int size, String event, String sender, String version){
		int i = indexForTimestamp(now);
		Record r = null;
		if( i >= 0 ){
			r = this.records[i];
			
		}else if( i == -1 ){
			r = farPast;
		}else if( i == -2 ){
			r = farFuture;
		}

		r.incrementCount();
		r.addSize(size);
		r.incrementEvent(event, size);
		r.incrementSender(sender, size);

		SizeAndCount v = messageVersions.get(version);
		if( v == null ){
			v = new LongRecord();
			messageVersions.put(version,v);
		}
		v.incrementCount();
		v.addSize(size);
	}

	@Override
	protected long zeroIntervalTimestamp(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}

	@Override
	protected boolean needForRotateStats(long time) {
		return( System.currentTimeMillis() > rotateSysdate );
	}
}
