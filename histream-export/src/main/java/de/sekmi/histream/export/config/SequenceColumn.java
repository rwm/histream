package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="sequence")
public class SequenceColumn extends AbstractColumn{
	@XmlElement(required=true)
	String id;
	
	@XmlElement
	Long start;
	
	long value;
	
	protected SequenceColumn(){
	}
	public SequenceColumn(String id){
		this.id = id;
	}
	
	@Override
	protected void initialize(){
		if( start != null ){
			value = start;
		}else{
			value = 1;
		}
	}
	@Override
	public Object getValueString() {
		return value;
		// TODO increment value somewhere else
	}
}
