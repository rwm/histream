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
