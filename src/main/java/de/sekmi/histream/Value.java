package de.sekmi.histream;

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

import javax.xml.bind.annotation.XmlEnum;

/**
 * @author marap1
 *
 * Interface for observation value. 
 * Also contains abnormal flag, value type and numeric operator.
 */
public interface Value {
	/**
	 * String value. Numeric values will be converted to String.
	 * @return String value
	 */
	String getValue();
	
	BigDecimal getNumericValue();
	
	Type getType();
	Operator getOperator();
	AbnormalFlag getAbnormalFlag();
	/**
	 * Value units. E.g. s or ml/h
	 * @return
	 */
	String getUnits();
	/**
	 * Lower reference value measured with the value
	 * @return reference value
	 */
	BigDecimal getReferenceLow();
	/**
	 * Upper reference value measured with the value
	 * @return reference value
	 */
	BigDecimal getReferenceHigh();
	
	
	@XmlEnum
	public enum Type{
		/**
		 * No value (getValue will return null)
		 */
		None,
		/**
		 * Text value
		 */
		Text,
		/** 
		 * Numeric value 
		 */
		Numeric/*, 
		/**
		 * Numeric enumeration value (needs lookup table)
		 */
		//NumericEnum
	}

	
	@XmlEnum
	public enum Operator{
		Equal("EQ"), 
		LessThan("L"), 
		LessOrEqual("LE"), 
		GreaterThan("G"), 
		GreaterOrEqual("GE"), 
		NotEqual("NE"), 
		Interval("I");
		
		private String acronym;
		private Operator(String acronym){
			this.acronym = acronym;
		}
		public String acronym(){return acronym;}
		
		public static Operator fromAcronym(String acronym){
			for( Operator o : values() )
				if( o.acronym.equals(acronym) )return o;
			throw new IllegalArgumentException("Invalid operator acronym: "+acronym);
		}
	}
}
