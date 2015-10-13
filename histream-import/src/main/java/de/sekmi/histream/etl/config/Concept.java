package de.sekmi.histream.etl.config;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

/**
 * Concept from a wide table
 * @author Raphael
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Concept{
	@XmlAttribute(required=true)
	String id;
	// TODO: value should contain also type (string,decimal,integer,...)
	Column<?> value;
	StringColumn unit;
	@XmlElement(required=true)
	DateTimeColumn start;
	DateTimeColumn end;
	@XmlElement(name="modifier")
	Modifier[] modifiers;
	// ...
	
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Modifier{
		@XmlAttribute(required=true)
		String id;
		// TODO: value with type
		Column<?> value;
		StringColumn unit;
		
		private Modifier(){
		}
		public Modifier(String id){
			this();
			this.id = id;
		}
	}
	
	private Concept(){
	}
	
	public Concept(String id, String startColumn, String format){
		this();
		this.id = id;
		this.start = new DateTimeColumn(startColumn, format);
	}
	
	protected Observation createObservation(String patid, String visit, ObservationFactory factory, ColumnMap map, Object[] row) throws ParseException{
		DateTimeAccuracy start = this.start.valueOf(map,row);
		Observation o = factory.createObservation(patid, this.id, start);
		if( visit != null ){
			o.setEncounterId(visit);
		}
		Object value = this.value.valueOf(map, row);
		String unit = null;
		if( this.unit != null ){
			unit = this.unit.valueOf(map, row);
		}
		// TODO: use type of column this.value to infer value type
		if( value == null ){
			// no value
			o.setValue(null);
		}else if( value instanceof String ){
			// string
			o.setValue(new StringValue((String)value));
			// TODO: set unit
		}else if( value instanceof BigDecimal ){
			// numeric
			NumericValue v = new NumericValue((BigDecimal)value,unit);
			o.setValue(v);
		}else if( value instanceof Long ){
			// numeric
			NumericValue v = new NumericValue((Long)value,unit);
			o.setValue(v);
		}else{
			throw new ParseException("Unsupported value type for concept id "+this.id+": "+value.getClass());
		}

		// TODO: modifiers
		
		return o;
	}
}