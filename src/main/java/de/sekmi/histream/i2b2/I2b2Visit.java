package de.sekmi.histream.i2b2;

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
