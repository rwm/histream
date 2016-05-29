package de.sekmi.histream.etl.scripting;

import java.util.Collection;
import java.util.Collections;

import jdk.nashorn.api.scripting.AbstractJSObject;

public class FactsArray extends AbstractJSObject{
	private Facts facts;
	
	public FactsArray(Facts facts){
		this.facts = facts;
	}
	@Override
	public Object getMember(String name) {
		return facts.get(name);
	}
	@Override
	public Object getSlot(int index) {
		return facts.get(index);
	}
	@Override
	public boolean hasMember(String name) {
		return facts.firstIndexOf(name) != -1;
	}
	@Override
	public boolean hasSlot(int slot) {
		return slot >= 0 && slot < facts.facts().size();
	}
	@Override
	public boolean isArray() {
		return true;
	}
	@Override
	public boolean isStrictFunction() {
		return true;
	}
	@Override
	public void removeMember(String name) {
		facts.remove(name);
	}
	@Override
	public Collection<Object> values() {
		return Collections.unmodifiableList(facts.facts());
	}

}
