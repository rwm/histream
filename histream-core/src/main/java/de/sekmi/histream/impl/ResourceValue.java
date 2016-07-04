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

/**
 * Resource value type. The value of this type indicates
 * a reference to an external resource.
 * TODO: how serialize this to i2b2?
 * 
 * @author R.W.Majeed
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name="resource")
public class ResourceValue extends AbstractValue{
	protected static final String EMPTY_STRING = "";

	@XmlValue
	private String value;
	// TODO XMLAttributes namespace/ns, ...

	public ResourceValue(){
		this.value = EMPTY_STRING;
	}
	public ResourceValue(String reference){
		this.value = reference;
	}

	@Override
	public boolean equals(Object other){
		if( !ResourceValue.class.equals(other.getClass()) )return false;
		if( other == this )return true;
		if( !equals((AbstractValue)other) )return false;
		return value.equals(((ResourceValue)other).value);
	}

	@Override
	public String getStringValue() {return value;}

	@Override
	public BigDecimal getNumericValue() {return null;}

	@Override // TODO native resource type, implement i2b2 serialisation
	public Type getType() {return Type.Text;}

	@Override
	public Operator getOperator() {return null;}

	@Override
	public BigDecimal getReferenceLow() {return null;}

	@Override
	public BigDecimal getReferenceHigh() {return null;}
	

}
