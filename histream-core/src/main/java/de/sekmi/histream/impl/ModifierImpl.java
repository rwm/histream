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


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import de.sekmi.histream.Modifier;
import de.sekmi.histream.Value;

@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({StringValue.class,NumericValue.class})
public class ModifierImpl implements Modifier {
	@XmlAttribute(name="code")
	private String modifierId;

	@XmlTransient
	private Value value;

	/**
	 * Constructor for JAXB
	 */
	protected ModifierImpl(){
	}
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

	/**
	 * Getter for JAXB
	 * @return abstract value
	 */
	@XmlElement(name="value")
	protected AbstractValue getAbstractValue(){
		return (AbstractValue)value;
	}
	protected void setAbstractValue(AbstractValue value){
		this.value = value;
	}

}
