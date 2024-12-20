package de.sekmi.histream.etl.config;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Value;
import de.sekmi.histream.etl.ColumnMap;
import de.sekmi.histream.etl.MapFeedback;
import de.sekmi.histream.etl.ParseException;
import de.sekmi.histream.ext.ExternalSourceType;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

public class EavTable extends Table<EavRow> {

	@XmlElement
	DataTableIdat idat;

	@XmlElement
	MDAT mdat;
	
	@XmlTransient
	Map<String,Column<?>> virtualColumnMap;
	
	@XmlElementWrapper(name="virtual")
	@XmlElement(name="value")
	public void setVirtualValueColumns(Column<?>[] values){
		if( values == null ){
			virtualColumnMap = null;
		}else{
			virtualColumnMap = new HashMap<>();
			for( Column<?> value : values ){
				virtualColumnMap.put(value.column, value);
			}
		}
	}
	/**
	 * Ignored columns
	 */
	@XmlElement
	Column<?>[] ignore;

	public Column<?>[] getVirtualValueColumns(){
		if( virtualColumnMap == null ){
			return null;
		}else{
			return virtualColumnMap.values().toArray(new Column<?>[virtualColumnMap.size()]);
		}
	}
	
	@XmlType(name="eav-mdat")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class MDAT{
		StringColumn concept;
		DateTimeColumn start;
		DateTimeColumn end;
		StringColumn location;
		StringColumn provider;
		StringColumn type;
		StringColumn value;
		StringColumn unit;
	}

	@Override
	public ColumnMap getColumnMap(String[] headers) throws ParseException {
		ColumnMap map = new ColumnMap(headers);
		if( idat.patientId == null ){
			throw new ParseException("datasource/eav-table/idat/patient-id column not specified");
			// TODO allow missing patient id, similar to WideTable.java
			// missing patient id is allowed, for cases 
			// when the visit id is unique and sufficient for matching

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
		if( mdat.provider != null ){
			map.registerColumn(mdat.provider);
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
		validateAllHeaders(headers, map, this.ignore);
		
		return map;
	}
	
	private Column<?> getVirtualColumn(String concept){
		return virtualColumnMap.get(concept);
	}

	@Override
	public EavRow fillRecord(ColumnMap colMap, Object[] row, ExternalSourceType source, String recordOrigin) throws ParseException {
		String patid = idat.patientId.valueOf(colMap, row);
		String visit = idat.visitId.valueOf(colMap, row);
		DateTimeAccuracy start = mdat.start.valueOf(colMap,row);
		String concept = mdat.concept.valueOf(colMap,row);
		String value = mdat.value.valueOf(colMap,row);
		String unit = mdat.unit.valueOf(colMap,row);
		Column<?> vcol = getVirtualColumn(concept);
		Object vval;
		if( vcol != null ){
			// use virtual column for value processing
			MapFeedback mf = new MapFeedback();
			vval = vcol.valueOf(value, mf);
			if( mf.hasConceptOverride() ){
				concept = mf.getConceptOverride();
			}
			if( mf.isActionDrop() ){
				return null; // ignore fact and row
			}
		}else if( value != null ){
			// no virtual column provided, parse value directly
			// use provided type info
			String type = null;
			if( mdat.type != null ){
				type = mdat.type.valueOf(colMap,row);
			}
			if( type == null ){
				// for now, use string
				// TODO determine type automatically from string representation
				vval = value;		
			}else if( type.equals(StringColumn.class.getAnnotation(XmlType.class).name()) ){
				vval = value;
			}else if( type.equals(DecimalColumn.class.getAnnotation(XmlType.class).name()) ){
				try{
					vval = new BigDecimal(value);
				}catch( NumberFormatException e ){
					throw new ParseException("Unable to parse number", e);
				}
			}else if( type.equals(IntegerColumn.class.getAnnotation(XmlType.class).name()) ){
				try{
					vval = Long.parseLong(value);
				}catch( NumberFormatException e ){
					throw new ParseException("Unable to parse integer", e);
				}
			}else{
				throw new ParseException("Unsupported value type: "+type);
			}
		}else{
			// null value
			vval = null;
		}

		EavRow eav = new EavRow(patid,visit,concept);
		eav.source = source;
		eav.recordOrigin = recordOrigin;

		if( start == null ){
			// start may be null at this point and will be filled later with the visit timestamp
			// see FactGroupingQueue#addFactsToWorkQueue(FactRow)
		}else {
			eav.start = start;
		}


		if( mdat.end != null ) {
			eav.end = mdat.end.valueOf(colMap,row);
		}

		if( mdat.location != null ) {
			eav.location = mdat.location.valueOf(colMap, row);
		}
		if( mdat.provider != null ) {
			eav.provider = mdat.provider.valueOf(colMap, row);
		}
		
		if( vval != null ){
			Value val;
			// convert native type to observation value
			if( vval instanceof String ){
				val = new StringValue((String)vval);
			}else if( vval instanceof BigDecimal ){
				val = new NumericValue((BigDecimal)vval,unit);
			}else if( vval instanceof Long ){
				val = new NumericValue((Long)vval,unit);
			}else{
				throw new ParseException("Internal error: unsupported native value type: "+vval.getClass());
			}
			eav.value = val;
			
		}// else fact without value

		return eav;
	}

}
