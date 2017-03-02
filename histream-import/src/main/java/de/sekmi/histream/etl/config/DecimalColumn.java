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
		if( input instanceof BigDecimal ){
			return (BigDecimal)input;
		}else{
			throw new ParseException("Invalid type for decimal column: "+input.getClass().getName());
		}
	}

	@Override
	public BigDecimal valueFromString(String input) throws ParseException {
		// remove leading/trailing spaces
		input = input.trim(); // TODO warning for user feedback if spaces were removed
		if( locale == null ){
			// parse according to BigDecimal(String)
			try{
				return new BigDecimal(input);
			}catch( NumberFormatException e ){
				throw new ParseException("Unable to parse number '"+input+"'", e);
			}
		}else{
			// use DecimalFormat for parsing
			if( decimalFormat == null ){
				decimalFormat = (DecimalFormat)NumberFormat.getNumberInstance(Locale.forLanguageTag(locale));
				decimalFormat.setParseBigDecimal(true);
			}
			try {
				return (BigDecimal)decimalFormat.parse(input);
			} catch (java.text.ParseException e) {
				throw new ParseException("Unable to parse number '"+input+"' with locale "+locale, e);
			}
		}
	}

}
