package de.sekmi.histream;

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
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.Date;

/**
 * Local date and time with specified accuracy. Maximum resolution is seconds.
 * Supported accuracy values are {@link ChronoUnit#YEARS}, {@link ChronoUnit#MONTHS},
 * {@link ChronoUnit#DAYS}, {@link ChronoUnit#HOURS}, {@link ChronoUnit#MINUTES} and
 * {@link ChronoUnit#SECONDS}.    
 * @author Raphael
 *
 */
public class DateTimeAccuracy implements Temporal {
	private LocalDateTime dateTime;
	private ChronoUnit accuracy;
	
	/**
	 * Create date time with accuracy to seconds.
	 * @param dateTime
	 */
	public DateTimeAccuracy(LocalDateTime dateTime){
		this.dateTime = dateTime;
		this.accuracy = ChronoUnit.SECONDS;
	}
	public DateTimeAccuracy(int year) {
		dateTime = LocalDateTime.of(year, 1, 1, 0, 0);
		accuracy = ChronoUnit.YEARS;
		// truncation works only up to days
		dateTime.truncatedTo(ChronoUnit.DAYS);
	}
	public DateTimeAccuracy(int year, int month) {
		dateTime = LocalDateTime.of(year, month, 1, 0, 0);
		accuracy = ChronoUnit.MONTHS;
		// truncation works only up to days
		dateTime.truncatedTo(ChronoUnit.DAYS);
	}
	public DateTimeAccuracy(int year, int month, int day) {
		dateTime = LocalDateTime.of(year, month, day, 0, 0);
		accuracy = ChronoUnit.DAYS;
		dateTime.truncatedTo(accuracy);
	}
	public DateTimeAccuracy(int year, int month, int day, int hours) {
		dateTime = LocalDateTime.of(year, month, day, hours, 0);
		accuracy = ChronoUnit.HOURS;
		dateTime.truncatedTo(accuracy);
	}
	public DateTimeAccuracy(int year, int month, int day, int hours, int mins) {
		dateTime = LocalDateTime.of(year, month, day, hours, mins);
		accuracy = ChronoUnit.MINUTES;
		dateTime.truncatedTo(accuracy);
	}
	public DateTimeAccuracy(int year, int month, int day, int hours, int mins, int secs) {
		dateTime = LocalDateTime.of(year, month, day, hours, mins, secs);
		accuracy = ChronoUnit.SECONDS;
		dateTime.truncatedTo(accuracy);
	}
	
	// Temporal interface behaves like undelaying dateTime
	@Override
	public long getLong(TemporalField arg0) {return dateTime.getLong(arg0);}
	@Override
	public boolean isSupported(TemporalField arg0) {return dateTime.isSupported(arg0);}
	@Override
	public boolean isSupported(TemporalUnit unit) {return dateTime.isSupported(unit);}
	@Override
	public Temporal plus(long amountToAdd, TemporalUnit unit) {return dateTime.plus(amountToAdd,unit);}
	@Override
	public long until(Temporal endExclusive, TemporalUnit unit) {return dateTime.until(endExclusive, unit);}
	@Override
	public Temporal with(TemporalField field, long newValue) {return dateTime.with(field,newValue);}

	/**
	 * Get the accuracy for the date time object.
	 * @return accuracy
	 */
	public ChronoUnit getAccuracy(){return accuracy;}
	
	/** 
	 * Set the accuracy for the date time object.
	 * TODO: what happens if the accuracy is increased (but the underlaying time was truncated)
	 * @param accuracy
	 */
	public void setAccuracy(ChronoUnit accuracy){
		this.accuracy = accuracy;
		// 
	}
	
	/**
	 * Get the local time
	 * @return
	 */
	public LocalDateTime getLocal(){ return dateTime; }
	
	public void set(Date timestamp, ChronoUnit accuracy){
		dateTime = LocalDateTime.from(timestamp.toInstant());
		dateTime.truncatedTo(accuracy);
		this.accuracy = accuracy;
	}
	
	@Override
	public String toString(){
		return toPartialIso8601();
	}
	
	/**
	 * Append exactly {@code digits} to the {@code builder}. Prefix with zeros if necessary.
	 * @param builder builder to append to
	 * @param field field to add
	 * @param digits digits to add
	 */
	private void appendWithZeroPrefix(StringBuilder builder, TemporalField field, int digits){
		int v = dateTime.get(field);
		for(int i=digits; i>1 && v<(10^(i-1)); i--){
			builder.append('0');
		}
		builder.append(v);
	}
	/**
	 * Convert the date to a partial ISO 8601 date time string.
	 * Information up to {@link #getAccuracy()}} is used for the
	 * string representation.
	 * @return partial date.
	 */
	public String toPartialIso8601(){
		StringBuilder b = new StringBuilder(20);
		if( dateTime == null )return "null";

		
		char[] prefixes = {0,'-','-','T',':',':'};
		ChronoField[] fields = {ChronoField.YEAR, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH, ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE};
		int[] digits = {4,2,2,2,2,2};
		
		for( int i=0; i<fields.length; i++ ){
			if( prefixes[i] != 0 )b.append(prefixes[i]);
			appendWithZeroPrefix(b, fields[i], digits[i]);
			if( accuracy == fields[i].getBaseUnit() )break;
		}
		
		return b.toString();
	}
	
	/**
	 * Parses a partial ISO 8601 date time string.
	 * [-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm]
	 * @param str
	 * @return date time with accuracy as derived from parse
	 * @throws IllegalArgumentException
	 */
	public static DateTimeAccuracy parsePartialIso8601(String str)throws IllegalArgumentException{
		if( str.length() < 4 )throw new IllegalArgumentException("Need at least 4 characters for year: "+str);
		// parse year
		int year = Integer.parseInt(str.substring(0, 4));
		if( str.length() == 4 ){ // specified to accuracy of years
			return new DateTimeAccuracy(year);
		}else if( str.length() < 7 || str.charAt(4) != '-' ){
			throw new IllegalArgumentException("Expected YYYY-MM");
		}
		// parse month
		int month = Integer.parseInt(str.substring(5, 7));
		if( str.length() == 7 ){ // specified to accuracy of months
			return new DateTimeAccuracy(year, month);
		}else if( str.length() < 10 || str.charAt(7) != '-' ){
			throw new IllegalArgumentException("Expected YYYY-MM-DD");
		}
		// parse day
		int day = Integer.parseInt(str.substring(8, 10));
		if( str.length() == 10 ){ // specified to accuracy of days
			return new DateTimeAccuracy(year, month, day);
		}else if( str.length() < 13 || str.charAt(10) != 'T' ){
			throw new IllegalArgumentException("Expected yyyy-mm-ddThh");
		}
		
		// parse hours
		int hours = Integer.parseInt(str.substring(11, 13));
		if( str.length() == 13 ){ // specified to accuracy of hours
			return new DateTimeAccuracy(year, month, day, hours);
		}else if( str.length() < 16 || str.charAt(13) != ':' ){
			throw new IllegalArgumentException("Expected yyyy-mm-ddThh:mm");
		}
		
		// parse minutes
		int mins = Integer.parseInt(str.substring(14, 16));
		if( str.length() == 16 ){ // specified to accuracy of minutes
			return new DateTimeAccuracy(year, month, day, hours, mins);
		}else if( str.length() < 19 || str.charAt(16) != ':' ){
			throw new IllegalArgumentException("Expected yyyy-mm-ddThh:mm:ss");
		}

		// parse seconds
		int secs = Integer.parseInt(str.substring(17, 19));
		if( str.length() == 19 ){ // specified to accuracy of seconds
			return new DateTimeAccuracy(year, month, day, hours, mins, secs);
		}else if( str.length() < 19 || str.charAt(16) != ':' ){
			throw new IllegalArgumentException("Expected yyyy-mm-ddThh:mm:ss");
		}
		throw new UnsupportedOperationException("Timezone support not implemented yet");
	}
}
