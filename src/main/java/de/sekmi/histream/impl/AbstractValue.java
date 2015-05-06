package de.sekmi.histream.impl;

import java.math.BigDecimal;

import de.sekmi.histream.AbnormalFlag;
import de.sekmi.histream.Value;

public abstract class AbstractValue implements Value{
	protected AbnormalFlag abnormalFlag;
	protected String units;
	
	public static AbstractValue NONE = new NilValue();
	
	@Override
	public AbnormalFlag getAbnormalFlag() {return abnormalFlag;}

	public void setAbnormalFlag(AbnormalFlag flag){
		this.abnormalFlag = flag;
	}
	
	@Override
	public String getUnits() {return units;}
	
	private static class NilValue extends AbstractValue{

		@Override
		public String getValue() {return null;}

		@Override
		public BigDecimal getNumericValue() {return null;}

		@Override
		public Type getType() {return Type.None;}

		@Override
		public Operator getOperator() {return null;}

		@Override
		public BigDecimal getReferenceLow() {return null;}

		@Override
		public BigDecimal getReferenceHigh() {return null;}
	}

}
