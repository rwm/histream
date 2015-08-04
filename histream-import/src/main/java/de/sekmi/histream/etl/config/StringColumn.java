package de.sekmi.histream.etl.config;

import de.sekmi.histream.etl.ParseException;

public class StringColumn extends Column<String>{

	public StringColumn(String name) {
		super(name);
	}
	protected StringColumn(){
		super();
	}
	@Override
	public String valueOf(Object input) throws ParseException {
		Object value = preprocessValue(input);
		if( value != null )return value.toString();
		else return null;
	}
	
}