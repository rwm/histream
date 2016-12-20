package de.sekmi.histream.etl;

import java.nio.file.Paths;


import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.io.GroupedXMLWriter;
import de.sekmi.histream.io.Streams;

/**
 * 
 * Read and process a specified import descriptor and output the HIStream XML format
 * 
 * @author R.W.Majeed
 *
 */
public class XMLExport {

	/**
	 * Read and process a specified import descriptor and output the HIStream XML format
	 * @param args single argument with file name
	 * @throws Exception error
	 */
	public static void main(String[] args) throws Exception{
		if( args.length != 1 ){
			System.err.println("Usage: XMLExport <import-descriptor-file>");
			System.exit(-1);
		}
		ETLObservationSupplier suppl = ETLObservationSupplier.load(Paths.get(args[0]).toUri().toURL());
		GroupedXMLWriter writer = new GroupedXMLWriter(System.out);
		Meta.transfer(suppl, writer);
		Streams.transfer(suppl, writer);
		suppl.close();
		writer.close();
	}
}
