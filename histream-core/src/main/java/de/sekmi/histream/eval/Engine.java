package de.sekmi.histream.eval;

import de.sekmi.histream.Observation;

public interface Engine {

	public boolean test(String expression, Observation fact)throws ScriptException;
}
