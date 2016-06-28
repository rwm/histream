package de.sekmi.histream.impl;

import de.sekmi.histream.Extension;
import de.sekmi.histream.Observation;
import de.sekmi.histream.ext.IdExtensionType;

/**
 * Extension to allow grouping/classification of facts.
 * This can be seen similar to the {@code class} attribute
 * in HTML which can be given to any element.
 * <p>
 * XXX still need to figure how to serialize the class id
 * to the {@code fact/@class} attribute.
 * </p>
 * 
 * @author R.W.Majeed
 *
 */
public class ClassExtension implements Extension<ClassExtension.Clazz> {


	public static class Clazz implements IdExtensionType{
		private String id;
		
		@Override
		public String getId() {
			return id;
		}

		@Override
		public void setId(String id) {
			this.id = id;
		}

	}

	@Override
	public Clazz createInstance(Observation observation) {
		// TODO use modifier "class" to get the class value
		// otherwise unable to create a class for the given observation
		throw new UnsupportedOperationException();
		//return null;
	}

	@Override
	public Clazz createInstance(Object... args) throws UnsupportedOperationException, IllegalArgumentException {
		if( args.length != 1 || !(args[0] instanceof String) ){
			throw new IllegalArgumentException("Fact extension for class needs exactly one string argument for the class id");
		}
		Clazz c = new Clazz();
		c.setId((String)args[0]);
		return c;
	}

	@Override
	public Class<?>[] getInstanceTypes() {
		return new Class[]{Clazz.class};
	}

}
