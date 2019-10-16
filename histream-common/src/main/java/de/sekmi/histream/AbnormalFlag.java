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


import javax.xml.bind.annotation.XmlEnum;

/**
 * 
 * @author marap1
 *
 * Typical values sent via HL7 at the university hospital in Giessen (total count during observation period):
 * <ul>
 * <li>L: 60000
 * <li>LL: 24000
 * <li>H: 43000
 * <li>HH: 43000
 * <li>*: 250
 * <li>**: 15
 * <li>N: 23000
 * <li>(empty/space): 14000
 * </ul>
 */
@XmlEnum
public enum AbnormalFlag{
	/**
	 * Abnormal (applies to non-numeric results)
	 */
	Abnormal 		("A"),
	/**
	 * Very abnormal (applies to non-numeric units, analogous to panic limits for numeric units)
	 */
	AbnormalOutsidePanicLimits 		("AA"),
	/**
	 * Above high normal
	 */
	AboveHighNormal 		("H"),
	/**
	 * Above upper panic limits
	 */
	AboveUpperPanicLimits 		("HH"),
	/**
	 * Above absolute high-off instrument scale
	 */
	HighOff (">"),
	/**
	 * Below absolute low-off instrument scale
	 */
	LowOff	("<"),
	/**
	 * Below low normal
	 */
	BelowNormal		("L"),
	/**
	 * Below lower panic limits
	 */
	BelowNormalPanicLimits		("LL"),
	/**
	 * Better--use when direction not relevant
	 */
	Better		("B"),
	/**
	 * Normal
	 */
	Normal		("N"),
	/**
	 * Significant change down
	 */
	DownSignificantly		("D"),
	/**
	 * Significant change up
	 */
	UpSignificantly		("U"),
	/**
	 * Worse--use when direction not relevant
	 */
	Worse		("W");
	
	
	
	
	String key;
	
	private AbnormalFlag(String key){
		this.key = key;
	}
	public String key(){return this.key;}
	
	/**
	 * Get abnormal flag for the specified key. For allowed values, see the enum values.
	 * @param key key string.
	 * @return null if key is null, matching flag otherwise
	 * @throws IllegalArgumentException for non-null keys which do not match any enum constant.
	 */
	public static final AbnormalFlag fromKey(String key)throws IllegalArgumentException{
		if( key == null )return null;
		for( AbnormalFlag v : AbnormalFlag.values() ){
			if( key.equals(v.key) )return v;
		}
		throw new IllegalArgumentException();
	}
	
}