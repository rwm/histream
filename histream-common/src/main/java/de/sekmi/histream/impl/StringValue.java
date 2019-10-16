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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name="string")
public class StringValue extends AbstractValue{
	protected static final String EMPTY_STRING = "";

	@XmlValue
	private String value;

	public StringValue(){
		this.value = EMPTY_STRING;
	}
	public StringValue(String value){
		this.value = value;
	}
	public StringValue(String value, String unit){
		this.value = value;
		this.units = unit;
	}

	@Override
	public boolean equals(Object other){
		if( !StringValue.class.equals(other.getClass()) )return false;
		if( other == this )return true;
		if( !equals((AbstractValue)other) )return false;
		return value.equals(((StringValue)other).value);
	}

	@Override
	public String getStringValue() {return value;}

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
