package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.ParseException;

@XmlType(name="string")
public class StringColumn extends Column<String>{

	public StringColumn(String name) {
		super(name);
	}
	protected StringColumn(){
		super();
	}
	@Override
	public String valueOf(Object input) throws ParseException {
		return input.toString();
	}
	@Override
	public String valueFromString(String input) throws ParseException {
		return input; // nothing to do
	}
	
}