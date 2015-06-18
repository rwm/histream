package de.sekmi.histream.ontology;

import java.util.Locale;

public interface EnumValue {
	String getPrefLabel(Locale locale);
	Object getValue();
}
