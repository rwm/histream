package de.sekmi.histream.impl;

import de.sekmi.histream.Modifier;
import de.sekmi.histream.Value;

public class ModifierImpl implements Modifier {
	private String modifierId;
	private Value value;
	
	public ModifierImpl(String modifierId){
		this.modifierId = modifierId;
	}
	@Override
	public String getConceptId() {
		return modifierId;
	}

	@Override
	public Value getValue() {
		return value;
	}

	@Override
	public void setValue(Value value) {
		this.value = value;
	}

}
