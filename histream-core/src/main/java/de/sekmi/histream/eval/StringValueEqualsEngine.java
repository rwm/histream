package de.sekmi.histream.eval;

import de.sekmi.histream.Observation;

/**
 * Evaluation engine for comparison of value to expression.
 * The evaluation returns true iif the expression is equal to the fact's string value.
 * <p>
 * This engine is stateless, thread-safe and does not need any resources or initialisation. Therefore
 * it can be used without constructing via {@link #ENGINE}.
 * 
 * @author Raphael
 *
 */
public class StringValueEqualsEngine implements Engine {

	private StringValueEqualsEngine(){
		
	}
	/**
	 * the engine
	 */
	public static final StringValueEqualsEngine ENGINE = new StringValueEqualsEngine();
	
	@Override
	public boolean test(String expression, Observation fact)
			throws ScriptException {
		if( fact.getValue() == null ){
			return expression == null;
		}else{
			return fact.getValue().getStringValue().equals(expression);
		}
	}

	@Override
	public void validateExpressionSyntax(String expression)
			throws ScriptException {
		// any string is valid
	}

}
