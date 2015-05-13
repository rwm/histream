package de.sekmi.histream.i2b2;

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


import de.sekmi.histream.impl.VisitImpl;


/**
 * I2b2 visit. The active encounter_ide is returned by {@link #getId()}.
 * 
 * @author Raphael
 *
 */
public class I2b2Visit extends VisitImpl {

	/**
	 * I2b2 internal encounter id (32bit integer)
	 */
	private int encounter_num;
	private int patient_num;
	
	/**
	 * String id aliases for the encounter
	 */
	String[] aliasIds;
	/**
	 * Index in aliasIds for the primary alias
	 */
	int primaryAliasIndex;
	
	int maxInstanceNum;
	
	public I2b2Visit(int encounter_num, int patient_num) {
		super();
		this.encounter_num = encounter_num;
		this.patient_num = patient_num;
		maxInstanceNum = 1;
		// TODO set startDate, endDate
		
	}

	public int getNum(){return encounter_num;}
	public int getPatientNum(){return patient_num;}
	
	@Override
	public String toString(){
		return "I2b2Visit(encounter_um="+encounter_num+")";
	}
}
