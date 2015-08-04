package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.ParseException;

@XmlType(name="integer")
public class IntegerColumn extends Column<Long> {

	@Override
	public Long valueOf(Object input) throws ParseException {
		input = preprocessValue(input);
		if( input == null ){
			return null;
		}else if( input instanceof String ){
			// TODO: use integerformat for parsing
			return Long.parseLong((String)input);
		}else if( input instanceof Integer ){
			return new Long((Integer)input);
		}else if( input instanceof Long ){
			return (Long)input;
		}else{
			throw new ParseException("Unsupported input type "+input.getClass().getName());
		}
	}
	
}
