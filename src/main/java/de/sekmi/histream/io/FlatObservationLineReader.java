package de.sekmi.histream.io;

import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Converts a stream of lines to flat observation events
 * @author Raphael
 *
 */
public class FlatObservationLineReader implements Consumer<String>{
	private FlatObservationWriter writer;
	private Pattern fieldSeparator;
	private static final int maxFields = 8;
	
	public FlatObservationLineReader(FlatObservationWriter writer){
		this.writer = writer;
		this.fieldSeparator = Pattern.compile("\\t");
	}

	@Override
	public void accept(String line) {
		if( line.length() == 0 ){
			// empty line
			return;
		}
		char first = line.charAt(0);
		
		if( first == '#' ){
			// comment
		}else if( first == '@' ){
			// command
			// TODO @meta, @concept, @group
		}else{
			String[] fields = fieldSeparator.split(line, maxFields);
			writer.writeObservation(fields);
		}
	}

}
