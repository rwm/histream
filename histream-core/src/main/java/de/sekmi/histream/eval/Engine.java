package de.sekmi.histream.eval;

import de.sekmi.histream.Observation;

/**
 * Engine for evaluating expressions for observations
 * 
 * @author Raphael
 *
 */
public interface Engine {

	/**
	 * Test the given expression for the given observation.
	 * @param expression expression to test
	 * @param fact fact to test
	 * @return whether the expression matches or not
	 * @throws ScriptException for syntax/evaluation errors
	 */
	public boolean test(String expression, Observation fact)throws ScriptException;
	
	/**
	 * Validates that the given expression is syntactically correct. 
	 * If the expression contains syntax errors, an exception is thrown.
	 * @param expression expression to validate
	 * @throws ScriptException syntax error
	 */
	public void validateExpressionSyntax(String expression)throws ScriptException;
}
