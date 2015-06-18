package de.sekmi.histream.ontology;

import java.util.regex.Pattern;

public interface ValueRestriction {
	Class<?> getType();
	EnumValue[] getEnumeration();
	Number minInclusive();
	Number maxInclusive();
	Integer minLength();
	Integer maxLength();
	Pattern getPattern();
}
