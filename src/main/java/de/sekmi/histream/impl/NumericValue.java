package de.sekmi.histream.impl;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.math.BigDecimal;

public class NumericValue extends AbstractValue {
	private BigDecimal value;
	private Operator operator;
	
	private BigDecimal referenceLow;
	private BigDecimal referenceHigh;
	

	public NumericValue(BigDecimal value){
		this(value, null);
	}
	public NumericValue(BigDecimal value, String units){
		this(value, units, Operator.Equal);
	}
	
	public NumericValue(BigDecimal value, String units, Operator operator){
		this.value = value;
		this.units = units;
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
