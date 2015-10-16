package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.ParseException;

@XmlType(name="integer")
public class IntegerColumn extends Column<Long> {

	@Override
	public Long valueOf(Object input) throws ParseException {
		if( input instanceof Integer ){
			return new Long((Integer)input);
		}else if( input instanceof Long ){
			return (Long)input;
		}else{
			throw new ParseException("Unsupported input type "+input.getClass().getName());
		}
	}

	@Override
	public Long valueFromString(String input) throws ParseException {
		return Long.parseLong(input);
	}
	
}
