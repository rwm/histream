package de.sekmi.histream.etl.config;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.etl.ParseException;

@XmlType(name="decimal")
public class DecimalColumn extends Column<BigDecimal>{
	@XmlTransient
	DecimalFormat decimalFormat;
	/**
	 * locale string for NumberFormat
	 */
	@XmlAttribute
	String locale;

	@Override
	public BigDecimal valueOf(Object input) throws ParseException {
		Object value = preprocessValue(input);
		if( value == null ){
			return null;
		}else if( value instanceof String ){
			if( locale == null ){
				// parse according to BigDecimal(String)
				try{
					return new BigDecimal((String)value);
				}catch( NumberFormatException e ){
					throw new ParseException("Unable to parse number: "+(String)value, e);
				}
			}else{
				// use DecimalFormat for parsing
				if( decimalFormat == null ){
					decimalFormat = (DecimalFormat)NumberFormat.getNumberInstance(Locale.forLanguageTag(locale));
					decimalFormat.setParseBigDecimal(true);
				}
				try {
					return (BigDecimal)decimalFormat.parse((String)value);
				} catch (java.text.ParseException e) {
					throw new ParseException("Unable to parse number: "+(String)value, e);
				}
			}
		}else if( value instanceof BigDecimal ){
			return (BigDecimal)value;
		}else{
			throw new ParseException("Invalid type for decimal column: "+value.getClass().getName());
		}
	}

}
