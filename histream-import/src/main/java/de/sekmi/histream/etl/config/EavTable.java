package de.sekmi.histream.etl.config;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.Value;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.EavRow;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

public class EavTable extends Table<EavRow> {

	@XmlElement
	DataTableIdat idat;

	@XmlElement
	MDAT mdat;
	
	@XmlType(name="eav-mdat")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class MDAT{
		StringColumn concept;
		DateTimeColumn start;
		DateTimeColumn end;
		StringColumn location;
		StringColumn type;
		StringColumn value;
		StringColumn unit;
	}

	@Override
	public ColumnMap getColumnMap(String[] headers) throws ParseException {
		ColumnMap map = new ColumnMap(headers);
		if( idat.patientId == null ){
			throw new ParseException("datasource/eav-table/idat/patient-id column not specified");
		}
		if( idat.visitId == null ){
			throw new ParseException("datasource/eav-table/idat/visit-id column not specified");
		}

		map.registerColumn(idat.patientId);
		map.registerColumn(idat.visitId);
		
		if( mdat.concept == null ){
			throw new ParseException("datasource/eav-table/mdat/concept column not specified");			
		}
		if( mdat.start == null ){
			throw new ParseException("datasource/eav-table/mdat/start column not specified");
		}
		map.registerColumn(mdat.concept);
		map.registerColumn(mdat.start);
		if( mdat.end != null ){
			map.registerColumn(mdat.end);
		}
		if( mdat.location != null ){
			map.registerColumn(mdat.location);
		}
		if( mdat.type != null ){
			map.registerColumn(mdat.type);
		}
		if( mdat.value != null ){
			map.registerColumn(mdat.value);
		}
		if( mdat.unit != null ){
			map.registerColumn(mdat.unit);
		}
		// make sure all columns are specified
		validateAllHeaders(headers, map, idat.ignore);
		
		return map;
	}

	@Override
	public EavRow fillRecord(ColumnMap map, Object[] row, ObservationFactory factory) throws ParseException {
		String patid = idat.patientId.valueOf(map, row);
		DateTimeAccuracy start = mdat.start.valueOf(map,row);
		String concept = mdat.concept.valueOf(map,row);
		Observation fact = factory.createObservation(patid, concept, start);

		String visit = idat.visitId.valueOf(map, row);
		if( visit != null ){
			fact.setEncounterId(visit);
		}
		String value = mdat.value.valueOf(map,row);
		if( value != null ){
			// generate/parse value
			String type = null;
			if( mdat.type != null ){
				type = mdat.type.valueOf(map,row);
			}
			Value factValue = null;
			if( type == null ){
				// for now, use string
				// TODO determine type automatically from string representation
				factValue = new StringValue(value);				
			}else if( type.equals(StringColumn.class.getAnnotation(XmlType.class).name()) ){
				factValue = new StringValue(value);
			}else if( type.equals(DecimalColumn.class.getAnnotation(XmlType.class).name())
					|| type.equals(IntegerColumn.class.getAnnotation(XmlType.class).name()) ){
				try{
					factValue = new NumericValue(new BigDecimal(value));
				}catch( NumberFormatException e ){
					throw new ParseException("Unable to parse number", e);
				}
			}
			fact.setValue(factValue);
		}
		
		return new EavRow(fact);
	}

}
