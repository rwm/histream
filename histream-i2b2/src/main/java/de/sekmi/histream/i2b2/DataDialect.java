package de.sekmi.histream.i2b2;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;

import de.sekmi.histream.AbnormalFlag;
import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Value;

/**
 * Configuration of exact meaning of values in
 * {@code observation_fact} table. E.g. how to
 * store/interpret null values.
 * <p>
 * The function calls beginning with {@code encode} produce
 * values which are stored in the database. The {@code decode} functions
 * are used to decode the database values and produce a usable value.
 * 
 * @author R.W.Majeed
 *
 */
public class DataDialect {
	private String nullProviderId;
	private String nullUnitCd;
	private String nullLocationCd;
	private String nullModifierCd;
	private String nullValueFlagCd;
	private String nullValueTypeCd;
	private int maxTvalLength;
	/** Timezone for timestamp / date time columns. */
	private ZoneId zoneId; // TODO may not be needed since the db always uses epoch seconds
	// TODO nullSexCd, nullInOutCd

	public DataDialect(){
		this.nullUnitCd = "@"; // technically, null is allowed, but the demodata uses both '@' and ''
		this.nullLocationCd = "@"; // technically, null is allowed, but the demodata only uses '@'
		this.nullValueFlagCd = "@";// technically, null is allowed, but the demodata uses both '@' and ''
		// TODO nullBlob (technically null allowed, but '' is used in demodata)
		this.nullModifierCd = "@"; // null not allowed, @ is used in demodata
		this.nullValueTypeCd = "@"; // TODO check database
		// null not allowed, use default 
		this.nullProviderId = "@";
		this.maxTvalLength = 255;
		this.zoneId = ZoneId.systemDefault();
	}
	void setDefaultProviderId(String providerId){
		this.nullProviderId = providerId;
	}
	public void setTimeZone(ZoneId zone){
		this.zoneId = zone;
	}
	public String getDefaultProviderId(){
		return nullProviderId;
	}
	
	public String getNullUnitCd(){
		return nullUnitCd;
	}
	
	public String getNullLocationCd(){
		return nullLocationCd;
	}
	public ZoneId getTimeZone(){
		return zoneId;
	}
	public String getNullModifierCd(){
		return nullModifierCd;
	}
	public String getNullValueFlagCd(){
		return nullValueFlagCd;
	}
	public String getNullValueTypeCd(){
		return nullValueTypeCd;
	}
	public int getMaxTvalLength() {
		return maxTvalLength;
	}
	public Timestamp encodeInstant(Instant instant){
		if( instant == null ){
			return null;
		}else{
			return Timestamp.from(instant);
			//return Timestamp.from(instant.atZone(zoneId).toLocalDateTime().atOffset(ZoneOffset.UTC).toInstant());
		}
	}
	public Timestamp encodeInstantPartial(DateTimeAccuracy instant, ZoneId zone){
		if( instant == null ){
			return null;
		}else { //if( zone != null ){
			return Timestamp.valueOf(instant.toLocal(zone));
//		}else {
//			return encodeInstant(instant.toInstantMin());
		}
	}
	public Instant decodeInstant(Timestamp timestamp){
		if( timestamp == null ){
			return null;
		}else{
			// XXX maybe use dialect.zoneId to 
			return timestamp.toInstant();
			//return timestamp.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime().atZone(zoneId).toInstant();
		}
	}
	public DateTimeAccuracy decodeInstantPartial(Timestamp timestamp){
		if( timestamp == null ){
			return null;
		}else{
			return new DateTimeAccuracy(decodeInstant(timestamp));
		}
	}
	private boolean isNullComparison(String value, String nullValue){
		if( value == null ){
			return true;
		}else if( nullValue != null && value.equals(nullValue) ){
			return true;
		}else{
			return false;
		}
	}
	public boolean isDefaultProviderId(String providerId){
		return isNullComparison(providerId, getDefaultProviderId());
	}
	public boolean isNullLocationCd(String locationCd){
		return isNullComparison(locationCd, getNullLocationCd());
	}
	private <T> T encodeNull(T value, T nullReplacement){
		return (value==null)?nullReplacement:value;
	}
	private <T> T decodeNull(T value, T nullReplacement){
		if( value == null ){
			return null;
		}else if( nullReplacement != null && nullReplacement.equals(value) ){
			return null;
		}else{
			return value;
		}
	}
	public String encodeLocationCd(String locationCd){
		return encodeNull(locationCd, getNullLocationCd());
	}
	public String encodeUnitCd(String unitCd){
		return encodeNull(unitCd, getNullUnitCd());
	}
	public String decodeUnitCd(String rowValue){
		return decodeNull(rowValue, nullUnitCd);
	}
	public String decodeValueTypeCd(String rowValue){
		return decodeNull(rowValue, nullValueTypeCd);
	}
	public String encodeProviderId(String providerId){
		return encodeNull(providerId, getDefaultProviderId());
	}
	public String decodeModifierCd(String rowValue){
		return decodeNull(rowValue, nullModifierCd);
	}
	public String decodeLocationCd(String rowValue){
		return decodeNull(rowValue, nullLocationCd);
	}
	
	public String encodeOperator(Value value){
		if( value.getOperator() == null )return "E";
		String op;
		switch( value.getOperator() ){
		case Equal:
			op = "E";
			break;
		case NotEqual:
			op = "NE";
			break;
		case LessThan:
			op = "L";
			break;
		case LessOrEqual:
			op = "LE";
			break;
		case GreaterThan:
			op = "G";
			break;
		case GreaterOrEqual:
			op = "GE";
			break;
		default:
			// TODO issue warning
			op = "E";
		}
		return op;
	}
	
	public String encodeValueFlagCd(Value value){
		String flag;
		if( value.getAbnormalFlag() == null )flag = null;
		else switch( value.getAbnormalFlag() ){
		case Abnormal:
			flag = "A";
			// TODO all flags
		default:
			flag = null;
		}
		return flag;
	}
	public AbnormalFlag decodeValueFlagCd(String value_flag_cd){
		if( value_flag_cd == null )return null;
		switch( value_flag_cd ){
		case "A":
			return AbnormalFlag.Abnormal;
			// TODO all flags
		default:
			return null;
		
		}
	}

}
