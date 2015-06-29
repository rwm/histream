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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.AbnormalFlag;
import de.sekmi.histream.Value;

@XmlTransient
public abstract class AbstractValue implements Value{
	@XmlAttribute(name="flag")
	protected AbnormalFlag flag;
	@XmlAttribute(name="unit")
	protected String units;
	
	public static AbstractValue NONE = new NilValue();
	
	@Override
	public AbnormalFlag getAbnormalFlag() {return flag;}

	public void setAbnormalFlag(AbnormalFlag flag){
		this.flag = flag;
	}
	
	@Override
	public String getUnits() {return units;}
	
	/**
	 * Compare whether the abstract properties defined in this class match
	 * @param o other abstract value to compare
	 * @return true when all abstract properties match, false otherwise
	 */
	protected boolean equals(AbstractValue o){
		if( !(o.units == null && this.units == null) && !(this.units != null && this.units.equals(o.units)) )return false;
		if( !(o.flag == null && this.flag == null) && !(this.flag != null && this.flag.equals(o.flag)) )return false;
		return true;
	}
	
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
