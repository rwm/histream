package de.sekmi.histream.i2b2;


import de.sekmi.histream.i2b2.ont.Import;

/**
 * Demo to load ontology from Eclipse workspace
 * @author marap1
 *
 */
public class Demo {

	public static void main(String args[]) throws Exception{
		
		Import.main(new String[]{"../HIStream-i2b2/examples/skos-ontology.properties","../HIStream-i2b2/examples/i2b2-ont-import.properties"});
	}
}
