package de.sekmi.histream.etl.config;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.ParseException;

@XmlType(name="decimal")
public class DecimalColumn extends Column<BigDecimal>{

	@Override
	public BigDecimal valueOf(Object input) throws ParseException {
		Object value = preprocessValue(input);
		if( value == null ){
			return null;
		}else if( value instanceof String ){
			// TODO: use decimalformat for parsing
			return new BigDecimal((String)value);
		}else if( value instanceof BigDecimal ){
			return (BigDecimal)value;
		}else{
			throw new ParseException("Invalid type for decimal column: "+value.getClass().getName());
		}
	}

}
