package histream.hl7filter.stats;

import histream.hl7.Message;
import histream.hl7.filter.MessageFilter;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/**
 * HL7 message statistics in hourly intervals.
 * 
 * @author marap1
 *
 */
public class StatisticsFilter implements MessageFilter {
	static final Logger log = Logger.getLogger(StatisticsFilter.class.getName());

	
	
	/**
	 * Earliest time specified in a MSH message
	 */
	protected Date minMSHTime;
	
	/**
	 * Latest time specified in a MSH message
	 */
	protected Date maxMSHTime;
	
	/**
	 * Current high-resolution statistics (per minute)
	 */
	protected MinuteStatistics minutes;
	
	/**
	 * Start time (epoch) for high resolution statistics
	 */
	protected long hourStart;
		
	/**
	 * Date/Time when the statistics module was started.
	 */
	protected Date startupTime;
	
	/**
	 * Number of messages, which could not be counted
	 * due to invalid timestamps or incomplete header fields
	 */
	protected long countInvalid;
	
	private Calendar cal;
	HourlyStatistics mshHourly;
	
	/**
	 * Constructs a statistics filter. The filter must
	 * be initialized by OSGi with activate() or by calling {@link #initStats(Date, int)}
	 */
	public StatisticsFilter(){
		this.cal = Calendar.getInstance();
		this.startupTime = cal.getTime();
	}
	
	
	/**
	 * Initialize the statistics filter.
	 * @param hiresMinutes Number of minutes to keep for minute statistics.
	 * @param maxNumDays Maximum number of days to keep track.
	 * @param daysInFuture Number of days of maxNumDays to use to count future events
	 * @param rotateAfterDays Number of days after which the logs are rotated. This is also the number of days for which oldest records are deleted to make room for new records.
	 */
	public void initStats(int hiresMinutes, int maxNumDays, int daysInFuture, int rotateAfterDays){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -(maxNumDays-daysInFuture+rotateAfterDays) -1);
		
		Date minimumDay = cal.getTime();
		
		
		this.minutes = new MinuteStatistics(hiresMinutes, System.currentTimeMillis());
		this.mshHourly = new HourlyStatistics((maxNumDays+rotateAfterDays)*24, minimumDay.getTime(), rotateAfterDays );
	}
	
	
	protected void activate(Map<String,Integer> config){
		Integer numDays = config.get("numDays");
		Integer futureDays = config.get("futureDays");
		Integer rotateDays = config.get("rotateDays");
		Integer hiresMins = config.get("hiresMinutes");
		
		// use default preferences if no configuration is available
		if( numDays == null )numDays = 90;
		if( futureDays == null )futureDays = 14;
		if( rotateDays == null )rotateDays = 7;
		if( hiresMins == null )hiresMins = 60;
		
		initStats(hiresMins, numDays, futureDays, rotateDays);
	}
	
	protected void deactivate(){
		
	}

	/**
	 * Calculates the number of characters for a 
	 * given HL7 message.
	 * 
	 * @param message
	 * @return message size in characters.
	 */
	protected int calculateMessageSize(Message message){
		int size = 0;
		for( int i=0; i<message.numSegments(); i++ ){
			String[] seg = message.getSegment(i);
			for( int j=0; j<seg.length; j++ ){
				size += seg[j].length();
			}
			// add field separator counts
			size += seg.length;
		}
		return size;
	}
	
	@Override
	public void filter(Message message) { 
		long now = System.currentTimeMillis();
		int size = calculateMessageSize(message);

		// highres statistics
		minutes.insertOne(now, size);
		
		// convert MSH time to java
		Date mshTime;
		String[] msh = message.getSegment(0);
		try{
			mshTime = message.getMessageTime();
		}catch( NumberFormatException e ){
			mshTime = null;
		}
		if( mshTime != null ){
			if( this.minMSHTime == null ){
				this.minMSHTime = mshTime;
				this.maxMSHTime = mshTime;
			}else if( mshTime.before(this.minMSHTime) )
				this.minMSHTime = mshTime;
			else if( mshTime.after(this.maxMSHTime) )
				this.maxMSHTime = mshTime;
			
			if( msh.length > 12 )
				mshHourly.insertOne(mshTime.getTime(), size, msh[9].substring(0,3), msh[3], msh[12]);
			else countInvalid ++;
			
		}else countInvalid ++;
	}
	
	

	@Override
	public int previewMSH(String[] msh) { 
		return 1; // pass all messages to the filter method
	}


	/*
	@Override
	public void getStatus(Report r) {
		Calendar cal = Calendar.getInstance();
		
		DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
		r.addSection("Overview");
		r.addElement("Startup time", df.format(startupTime));
		r.addElement("Unparsable messages", countInvalid);

		if( this.minMSHTime != null ){
			r.addElement("Min. MSH time", df.format(this.minMSHTime));
			r.addElement("Max. MSH time", df.format(this.maxMSHTime));
		}

		r.addSection("Messages received (Date,Count,Speed)");
		
		df = DateFormat.getTimeInstance(DateFormat.SHORT);
		cal.setTimeInMillis(minutes.getStartingTime());

		NumberFormat nf = new DecimalFormat("0.00");

		for( int i=0; i<minutes.getNumIntervals(); i++ ){
			MinuteStatistics.Record rec = minutes.records[i];
			if( rec.count > 0 ){
				float speed = (rec.size / (float)minutes.getIntervalInMillis());

				r.addElement(df.format(cal.getTime()), new Object[]{rec.count,nf.format(speed)});
				
			}
			cal.add(Calendar.MILLISECOND, (int)minutes.getIntervalInMillis());
		}

		
		df = new java.text.SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

		r.addSection("HL7 statistics by header segment (count,size)");
				
		Enumeration<String> e = mshHourly.messageVersions.keys();
		while( e.hasMoreElements() ){
			String key = e.nextElement();
			SizeAndCount rec = mshHourly.messageVersions.get(key);
		
			r.addElement(key, new Object[]{rec.getCount(),humanReadableSize(rec.getSize())});
		}
		/*

		cal.setTimeInMillis(mshHourly.getStartingTime());
		b.append("<table border=\"1\"><tr><th>Date</th><th>Count</th><th>Size</th><th>Speed</th><th>Msg.Types</th><th>Senders</th></tr>\n");
		for( int i=0; i<mshHourly.getNumIntervals(); i++){
			HourlyStatistics.Record r = mshHourly.records[i];
			if( r.getCount() > 0 ){
				float speed = (r.getSize() / (1000.0f*60*60));
				
				b.append("<tr><td>"+df.format(cal.getTime())+"</td><td align=\"right\">"+r.getCount()+"</td><td align=\"right\">"+r.getSize()+"</td><td align=\"right\">"+nf.format(speed)+"</td>\n");
				b.append("<td>"+HourlyStatistics.hashtableCounts(r.events)+"</td><td>"+HourlyStatistics.hashtableCounts(r.senders)+"</td></tr>");
			}
			cal.add(Calendar.MILLISECOND, (int)mshHourly.getIntervalInMillis());
			
		}
		b.append("</table></p>");

	}*/

	/**
	 * Provides a more human readable representation of a large size.
	 * @param size possibly large size in bytes
	 * @return human readable representation.
	 */
	public static String humanReadableSize(long size){
		float f = size;
		String unit="B";
		if( f > 1e10 ){
			f /= 1e9;
			unit = "GB";
		}else if( f > 1e7){
			f /= 1e6;
			unit = "MB";
		}else if( f > 1e4 ){
			f /= 1e3;
			unit = "KB";
		}
		return Float.toString(f)+unit;
	}


}
