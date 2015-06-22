package de.sekmi.histream.ontology;

public class EnumValue {
	private String label;
	private Object value;

	public EnumValue(String label, Object value){
		this.label = label;
		this.value = value;
	}

	public String getPrefLabel(){return label;}
	public Object getValue(){return value;}
}
