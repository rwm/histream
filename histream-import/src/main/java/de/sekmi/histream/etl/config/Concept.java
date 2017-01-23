package de.sekmi.histream.etl.config;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.MapFeedback;
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

	private Value createObservationValue(Object val, String unit) throws ParseException{
		if( val == null ){
			// no value
			return null;
		}else if( val instanceof String ){
			// string
			return new StringValue((String)val, unit);
		}else if( val instanceof BigDecimal ){
			// numeric
			return new NumericValue((BigDecimal)val, unit);
		}else if( val instanceof Long ){
			// numeric
			return new NumericValue((Long)val, unit);
		}else{
			throw new ParseException("Unsupported value type for concept id "+this.id+": "+val.getClass());
		}
	}
	/**
	 * Create an observation for this concept with the given row data.
	 * TODO allow mapping actions to happen at this place, e.g. drop concept, log warning, change value
	 * 
	 * @param patid patient id
	 * @param visit visit id
	 * @param factory observation factory
	 * @param map column map
	 * @param row row data
	 * @return fact
	 * @throws ParseException parse 
	 */
	protected Observation createObservation(String patid, String visit, ObservationFactory factory, ColumnMap map, Object[] row) throws ParseException{
		MapFeedback mf = new MapFeedback();

		DateTimeAccuracy start = this.start.valueOf(map,row, mf);
		mf.resetValue();
		String concept = this.id;

		// parse value
		String unit = null;
		if( this.unit != null ){
			unit = this.unit.valueOf(map, row, mf);
			mf.resetValue();
		}

		// TODO: use type of column this.value to infer value type
		Object val = null;
		if( this.value != null ){
//			Objects.requireNonNull(this.value, "No value for concept: "+id);
			val = this.value.valueOf(map, row, mf);
			mf.resetValue();
		}

		if( mf.hasConceptOverride() ){
			concept = mf.getConceptOverride();
		}
		if( mf.isActionDrop() ){
			return null; // ignore this fact
		}

		// if start is null/na, use visit start timestamp
		if( start == null ){
			// start may be null at this point and will be filled later with the visit timestamp
			// see FactGroupingQueue#addFactsToWorkQueue(FactRow)
		}

		Observation o = factory.createObservation(patid, concept, start);
		o.setValue(createObservationValue(val, unit));

		// load modifiers
		if( modifiers != null ){
			for( int i=0; i<modifiers.length; i++ ){
				mf = new MapFeedback();
				Modifier m = modifiers[i];
				// parse value
				val = null;
				if( m.value != null ){
					val = m.value.valueOf(map, row, mf);
					mf.resetValue();
				}
				// parse unit
				unit = null;
				if( m.unit != null ){
					unit = m.unit.valueOf(map, row, mf);
					mf.resetValue();
				}
				concept = m.id;
				// modifier values can override the modifier-ids via concept override
				if( mf.hasConceptOverride() ){
					concept = mf.getConceptOverride();
				}
				// or drop the modifier
				if( mf.isActionDrop() ){
					continue; // ignore this modifier
				}
				o.addModifier(concept, createObservationValue(val, unit));
			}
		}

		if( visit != null ){
			o.setEncounterId(visit);
		}

		return o;
	}
}