package de.sekmi.histream.impl;

import java.math.BigDecimal;

public class StringValue extends AbstractValue{
	private String value;

	public StringValue(String value){
		this.value = value;
	}
	

	@Override
	public String getValue() {return value;}

	@Override
	public BigDecimal getNumericValue() {return null;}

	@Override
	public Type getType() {return Type.Text;}

	@Override
	public Operator getOperator() {return null;}

	@Override
	public BigDecimal getReferenceLow() {return null;}

	@Override
	public BigDecimal getReferenceHigh() {return null;}
	

}
