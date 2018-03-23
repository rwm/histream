package de.sekmi.histream;

import java.text.ParseException;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Instant;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.sekmi.histream.xml.DateTimeAccuracyAdapter;

/**
 * Local date and time with specified accuracy. Maximum resolution is seconds.
 * For supported accuracy, see {@link #setAccuracy(ChronoUnit)}.
 * @author R.W.Majeed
 *
 */
@XmlJavaTypeAdapter(DateTimeAccuracyAdapter.class)
public class DateTimeAccuracy implements Comparable<DateTimeAccuracy> {
	static final String PARTIAL_FORMATTER_PATTERN = "u[-M[-d['T'H[:m[:s[.SSS]]][X]]]]";
	static final DateTimeFormatter PARTIAL_FORMATTER  = DateTimeFormatter.ofPattern(PARTIAL_FORMATTER_PATTERN);

	// TODO why not use instant, since we always calculate UTC? or Offset/ZonedDateTime?
	private Instant instant;
	private ChronoUnit accuracy;
	
	/**
	 * Create date time with accuracy to seconds.
	 * @param instant timestamp
	 */
	public DateTimeAccuracy(Instant instant){
		this.instant = instant;
		this.accuracy = ChronoUnit.SECONDS;
	}
	@Deprecated
	public DateTimeAccuracy(ZoneId zone, int year) {
		instant = LocalDateTime.of(year, 1, 1, 0, 0).atZone(zone).toInstant();
		accuracy = ChronoUnit.YEARS;
	}
	@Deprecated
	public DateTimeAccuracy(ZoneId zone, int year, int month) {
		instant = LocalDateTime.of(year, month, 1, 0, 0).atZone(zone).toInstant();
		accuracy = ChronoUnit.MONTHS;
	}
	@Deprecated
	public DateTimeAccuracy(ZoneId zone, int year, int month, int day) {
		instant = LocalDateTime.of(year, month, day, 0, 0).atZone(zone).toInstant();
		accuracy = ChronoUnit.DAYS;
	}
	@Deprecated
	public DateTimeAccuracy(ZoneId zone, int year, int month, int day, int hours) {
		instant = LocalDateTime.of(year, month, day, hours, 0).atZone(zone).toInstant();
		accuracy = ChronoUnit.HOURS;
	}
	@Deprecated
	public DateTimeAccuracy(ZoneId zone, int year, int month, int day, int hours, int mins) {
		instant = LocalDateTime.of(year, month, day, hours, mins).atZone(zone).toInstant();
		accuracy = ChronoUnit.MINUTES;
	}
	@Deprecated
	public DateTimeAccuracy(ZoneId zone, int year, int month, int day, int hours, int mins, int secs) {
		instant = LocalDateTime.of(year, month, day, hours, mins, secs).atZone(zone).toInstant();
		accuracy = ChronoUnit.SECONDS;
	}
	
	/**
	 * Convert the partial date time to an instant.
	 * Will return the minimum instant for the given accuracy.
	 * E.g. accuracy of YEAR will return the the first second in the given year.
	 * @return minimum instant within the given accuracy
	 */
	public Instant toInstantMin(){
		return instant;
	}
	// TODO toInstantMax() (increase field at accuracy and subtract one millisecond)
	
	/**
	 * Get the accuracy for the date time object.
	 * <p>
	 * Supported accuracy values are {@link ChronoUnit#YEARS}, {@link ChronoUnit#MONTHS},
     * {@link ChronoUnit#DAYS}, {@link ChronoUnit#HOURS}, {@link ChronoUnit#MINUTES} and
     * {@link ChronoUnit#SECONDS}
     * <p>
	 * @return accuracy
	 */
	public ChronoUnit getAccuracy(){return accuracy;}
	
	/** 
	 * Set the accuracy for the date time object.
	 * <p>
	 * Supported accuracy values are {@link ChronoUnit#YEARS}, {@link ChronoUnit#MONTHS},
     * {@link ChronoUnit#DAYS}, {@link ChronoUnit#HOURS}, {@link ChronoUnit#MINUTES} and
     * {@link ChronoUnit#SECONDS}
     * <p>
	 * TODO: what happens if the accuracy is increased (but the underlaying time was truncated)
	 * @param accuracy accuracy
	 */
	public void setAccuracy(ChronoUnit accuracy){
		this.accuracy = accuracy;
		// 
	}
	
//	/**
//	 * Get the local time
//	 * @return local time
//	 */
	//public LocalDateTime getLocal(){ return instant.at; }
	
	public void set(Date timestamp, ChronoUnit accuracy){
		instant = timestamp.toInstant();
		this.accuracy = accuracy;
	}
	
	@Override
	public String toString(){
		return toPartialIso8601(ZoneOffset.UTC.normalized());
	}
	
	/**
	 * Append exactly {@code digits} to the {@code builder}. Prefix with zeros if necessary.
	 * @param builder builder to append to
	 * @param field field to add
	 * @param digits digits to add
	 */
	private static void appendWithZeroPrefix(StringBuilder builder, TemporalAccessor date, TemporalField field, int digits){
		padZeros(builder,date.get(field), digits);
	}
	private static void padZeros(StringBuilder builder, int value, int digits){
		int pow = 1;
		for( int i=1; i<digits; i++ )pow *= 10;
		while( value < pow && pow > 1 ){
			builder.append('0');
			pow /= 10;
		}
		builder.append(value);		
	}
	/**
	 * Convert the date to a partial ISO 8601 date time string.
	 * Information up to {@link #getAccuracy()}} is used for the
	 * string representation.
	 * <p>
	 * Output is the same as {@link #toPartialIso8601(ZoneId)} with {@code null} argument.
	 * </p>
	 * @return partial date.
	 */
	public String toPartialIso8601(){
		return toPartialIso8601(null);
	}
	/**
	 * Convert the date to a partial ISO 8601 date time string.
	 * Information up to {@link #getAccuracy()}} is used for the
	 * string representation.
	 * @param tz time zone id. Can be {@code null} to omit the zone information
	 * @return partial date.
	 */
	public String toPartialIso8601(ZoneId tz){
		StringBuilder b = new StringBuilder(20);
		if( instant == null )return "null";

		TemporalAccessor dt;
		if( tz != null ){
			// use timezone information.
			// Assume that dateTime is given in UTC. For output convert to destination timezone.
			dt = instant.atOffset(ZoneOffset.UTC).atZoneSameInstant(tz);
		}else{
			// no zone info, output will not have offset
			dt = instant.atOffset(ZoneOffset.UTC).toLocalDateTime();
		}
		
		char[] prefixes = {0,'-','-','T',':',':'};
		ChronoField[] fields = {ChronoField.YEAR, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH, ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE};
		int[] digits = {4,2,2,2,2,2};
		int i;
		for( i=0; i<fields.length; i++ ){
			if( prefixes[i] != 0 )b.append(prefixes[i]);
			appendWithZeroPrefix(b, dt, fields[i], digits[i]);
			if( accuracy == fields[i].getBaseUnit() )break;
		}
		if( tz != null && i >= 3 ){
			// hours present
			// add zone offset
			int os = ((ZonedDateTime)dt).getOffset().getTotalSeconds();
			if( os == 0 ){
				// output Z
				b.append('Z');
			}else{
				// append sign and four characters
				if( os < 0 ){
					b.append('-');
				}else{
					b.append('+');
				}
				// hours
				int ox = os / 3600;
				os = os % 3600;
				padZeros(b,ox,2);
				// minutes
				ox = os / 60;
				padZeros(b,ox,2);
				// ignore seconds, not part of ISO
			}
		}
		
		return b.toString();
	}

	/**
	 * Parses a partial ISO 8601 date time string.
	 * [-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hhmm]
	 * <p>
	 * At least the year must be specified. All other fields can be left out.
	 * If no zone offset is specified, UTC is assumed.
	 * </p>
	 * @param str ISO 8601 string
	 *  If {@code null} is specified, no offset adjustments are done, with same result as UTC
	 * @return date time with accuracy as derived from parse
	 * @throws ParseException for unparsable string
	 * @throws IllegalArgumentException unparsable string (old unchecked exception)
	 */
	@Deprecated
	public static DateTimeAccuracy parsePartialIso8601(String str)throws ParseException{
		return parsePartialIso8601(str, null);
	}

	/**
	 * Parses a partial ISO 8601 date time string.
	 * [-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hhmm]
	 * <p>
	 * At least the year must be specified. All other fields can be left out.
	 * </p>
	 * @param str ISO 8601 string
	 * @param localZone time zone to use for offset if no offset is specified. 
	 *  If {@code null} is specified, no offset adjustments are done, with same result as UTC
	 * @return date time with accuracy as derived from parse
	 * @throws ParseException for unparsable string
	 * @throws IllegalArgumentException unparsable string (old unchecked exception)
	 */
	public static DateTimeAccuracy parsePartialIso8601(String str, ZoneId localZone)throws ParseException{
		ParsePosition pos = new ParsePosition(0);
		TemporalAccessor a = PARTIAL_FORMATTER.parseUnresolved(str, pos);
		// first check that everything was parsed
		if( pos.getErrorIndex() != -1 ){
			throw new ParseException("Parse error at position "+pos.getErrorIndex(), pos.getErrorIndex());
		}else if( pos.getIndex() != str.length() ){
			throw new ParseException("Unparsed text found at index "+pos.getIndex()+": "+str.substring(pos.getIndex()), pos.getIndex());
		}
		// everything parsed without error
		// now check for accuracy
		ChronoUnit accuracy;
		LocalDateTime dateTime;
		if( a.isSupported(ChronoField.MILLI_OF_SECOND) ){
			// maximum accuracy of nanoseconds
			// not supported yet, truncate to seconds
			accuracy = ChronoUnit.MILLIS;
//			dateTime = LocalDateTime.from(a);
			dateTime = LocalDateTime.of(a.get(ChronoField.YEAR), a.get(ChronoField.MONTH_OF_YEAR), a.get(ChronoField.DAY_OF_MONTH), a.get(ChronoField.HOUR_OF_DAY), a.get(ChronoField.MINUTE_OF_HOUR), a.get(ChronoField.SECOND_OF_MINUTE), a.get(ChronoField.NANO_OF_SECOND));
		}else if( a.isSupported(ChronoField.SECOND_OF_MINUTE) ){
			accuracy = ChronoUnit.SECONDS;
			dateTime = LocalDateTime.of(a.get(ChronoField.YEAR), a.get(ChronoField.MONTH_OF_YEAR), a.get(ChronoField.DAY_OF_MONTH), a.get(ChronoField.HOUR_OF_DAY), a.get(ChronoField.MINUTE_OF_HOUR), a.get(ChronoField.SECOND_OF_MINUTE));
		}else if( a.isSupported(ChronoField.MINUTE_OF_HOUR) ){
			accuracy = ChronoUnit.MINUTES;
			dateTime = LocalDateTime.of(a.get(ChronoField.YEAR), a.get(ChronoField.MONTH_OF_YEAR), a.get(ChronoField.DAY_OF_MONTH), a.get(ChronoField.HOUR_OF_DAY), a.get(ChronoField.MINUTE_OF_HOUR));
		}else if( a.isSupported(ChronoField.HOUR_OF_DAY) ){
			accuracy = ChronoUnit.HOURS;
			dateTime = LocalDateTime.of(a.get(ChronoField.YEAR), a.get(ChronoField.MONTH_OF_YEAR), a.get(ChronoField.DAY_OF_MONTH), a.get(ChronoField.HOUR_OF_DAY), 0);
		}else if( a.isSupported(ChronoField.DAY_OF_MONTH) ){
			accuracy = ChronoUnit.DAYS;
			dateTime = LocalDateTime.of(a.get(ChronoField.YEAR), a.get(ChronoField.MONTH_OF_YEAR), a.get(ChronoField.DAY_OF_MONTH), 0, 0);
		}else if( a.isSupported(ChronoField.MONTH_OF_YEAR) ){
			dateTime = LocalDateTime.of(a.get(ChronoField.YEAR), a.get(ChronoField.MONTH_OF_YEAR), 1, 0, 0);
			accuracy = ChronoUnit.MONTHS;
		}else{
			// format requires at least year
			accuracy = ChronoUnit.YEARS;
			dateTime = LocalDateTime.of(a.get(ChronoField.YEAR), 1, 1, 0, 0);
		}
		// check for zone offset
		ZoneOffset off = null;
		Instant inst;
		if( a.isSupported(ChronoField.OFFSET_SECONDS) ){
			off = ZoneOffset.ofTotalSeconds(a.get(ChronoField.OFFSET_SECONDS));
			// adjust to UTC
			//inst = dateTime.atOffset(off).withOffsetSameInstant(ZoneOffset.UTC).toInstant();
			inst = dateTime.atOffset(off).toInstant();
		}else if( localZone != null ){
			// use specified local zone
			//dateTime = dateTime.atZone(localZone).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
			inst = dateTime.atZone(localZone).toInstant();
		}else{
			// no zone in input and no default zone
			// treat as UTC
			inst = dateTime.atOffset(ZoneOffset.UTC).toInstant();
		}
		DateTimeAccuracy me = new DateTimeAccuracy(inst);
		me.accuracy = accuracy;
		return me;
	}
	
	/**
	 * Parse date time with a formatter.
	 * 
	 * @param formatter formatter
	 * @param text input text
	 * @param zoneId time zone to use, if the parser doesn't supply a time zone or offset
	 * @return date time with accuracy
	 */
	public static DateTimeAccuracy parse(DateTimeFormatter formatter, CharSequence text, ZoneId zoneId){
		ParsePosition pos = new ParsePosition(0);
		TemporalAccessor a = formatter.parseUnresolved(text, pos);
		if( pos.getErrorIndex() != -1 ){
			throw new DateTimeParseException("Text '"+String.valueOf(text.charAt(pos.getErrorIndex()))+"' could not be parsed at index "+pos.getErrorIndex(), text, pos.getErrorIndex());
		}else if( pos.getIndex() != text.length() ){
			throw new DateTimeParseException("Unparsed text found at index "+pos.getIndex(), text, pos.getIndex());
		}
		
		try{
			int offset = a.get(ChronoField.OFFSET_SECONDS);
			// explicit offset specified, use that information
			zoneId = ZoneOffset.ofTotalSeconds(offset);
		}catch( DateTimeException e ){
			// no offset available
			// use default specified in zoneId param
		}
		int year = a.get(ChronoField.YEAR);
		// month
		int month;
		try{
			month = a.get(ChronoField.MONTH_OF_YEAR);
		}catch( DateTimeException e ){
			return new DateTimeAccuracy(zoneId, year);
		}
		
		int day;
		try{
			day = a.get(ChronoField.DAY_OF_MONTH);
		}catch( DateTimeException e ){
			return new DateTimeAccuracy(zoneId, year,month);
		}

		int hour;
		try{
			hour = a.get(ChronoField.HOUR_OF_DAY);
		}catch( DateTimeException e ){
			return new DateTimeAccuracy(zoneId, year,month,day);
		}

		int minute;
		try{
			minute = a.get(ChronoField.MINUTE_OF_HOUR);
		}catch( DateTimeException e ){
			return new DateTimeAccuracy(zoneId, year,month,day, hour);
		}
		
		int seconds;
		try{
			seconds = a.get(ChronoField.SECOND_OF_MINUTE);
		}catch( DateTimeException e ){
			return new DateTimeAccuracy(zoneId, year,month,day, hour, minute);
		}

		return new DateTimeAccuracy(zoneId, year,month,day, hour, minute, seconds);
		// milliseconds not supported for now
	}
	@Override
	public int compareTo(DateTimeAccuracy o) {
		int cmp = instant.compareTo(o.instant);
		// if instants are equal, order by accuracy. more accurate comes first
		if( cmp == 0 ){
			cmp = accuracy.compareTo(o.accuracy);
		}
		return cmp;
	}
	
	@Override
	public int hashCode() {
		final int prime = 37;
		int result = 1;
		result = prime * result + ((accuracy == null) ? 0 : accuracy.hashCode());
		result = prime * result + ((instant == null) ? 0 : instant.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object other){
		Objects.requireNonNull(other);
		if( other.getClass() != DateTimeAccuracy.class )return false;
		DateTimeAccuracy o = (DateTimeAccuracy)other;
		if( !o.accuracy.equals(this.accuracy) )return false;
		return instant.equals(o.instant);
	}

}
