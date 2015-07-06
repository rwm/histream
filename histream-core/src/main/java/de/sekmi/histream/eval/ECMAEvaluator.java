package de.sekmi.histream.eval;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import de.sekmi.histream.Observation;

/**
 * Evaluate ECMAScript/Javascript expressions with {@link Observation}.
 * 
 * @author Raphael
 *
 */
public class ECMAEvaluator implements Engine{

	private ScriptEngine engine;
	
	public ECMAEvaluator(){
		ScriptEngineManager m = new ScriptEngineManager();
		this.engine = m.getEngineByName("nashorn");
		// TODO: add context to control reader/writer
		//this.engine.setContext(this);
		
		
	}
	
	@Override
	public boolean test(String expression, Observation fact)
			throws ScriptException {
	
		Bindings bindings = engine.createBindings();
		bindings.put("fact", fact);
		Object ret;
		try {
			ret = engine.eval(expression, bindings);
		} catch (javax.script.ScriptException e) {
			throw new ScriptException(e);
		}
		if( ret == null || !(ret instanceof Boolean) ){
			throw new ScriptException("Expression did not return Boolean: "+expression);
		}
		return ((Boolean)ret).booleanValue();
	}

}
