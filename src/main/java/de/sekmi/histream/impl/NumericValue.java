package de.sekmi.histream.impl;

import java.math.BigDecimal;

public class NumericValue extends AbstractValue {
	private BigDecimal value;
	private Operator operator;
	
	private BigDecimal referenceLow;
	private BigDecimal referenceHigh;
	

	public NumericValue(BigDecimal value){
		this(value, Operator.Equal);
	}
	
	public NumericValue(BigDecimal value, Operator operator){
		this.value = value;
		this.operator = operator;		
	}
	
	public void setReferenceRange(BigDecimal referenceLow, BigDecimal referenceHigh){
		this.referenceLow = referenceLow;
		this.referenceHigh = referenceHigh;
	}
	
	
	
	@Override
	public String getValue() {return value.toString();}

	@Override
	public BigDecimal getNumericValue() {return value;}

	@Override
	public Type getType() {return Type.Numeric;}

	@Override
	public Operator getOperator() {return operator;}

	@Override
	public BigDecimal getReferenceLow() {return referenceLow;}

	@Override
	public BigDecimal getReferenceHigh() {return referenceHigh;}

}
