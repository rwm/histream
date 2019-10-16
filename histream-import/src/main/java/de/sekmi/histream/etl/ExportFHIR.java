package de.sekmi.histream.etl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;

import javax.xml.stream.XMLStreamException;

import de.sekmi.histream.impl.Meta;
import de.sekmi.histream.io.GroupedFhirBundleWriter;
import de.sekmi.histream.io.GroupedXMLWriter;
import de.sekmi.histream.io.Streams;

/**
 * 
 * Read and process a specified import descriptor and output the HIStream XML format
 * 
 * @author R.W.Majeed
 *
 */
public class ExportFHIR {

	/**
	 * Read and process a specified import descriptor and output the HIStream XML format
	 * @param args single argument with file name
	 * @throws Exception error
	 */
	public static void main(String[] args) throws Exception{
		if( args.length != 1 ){
			System.err.println("Usage: ExportFHIR <import-descriptor-file>");
			System.exit(-1);
		}
		descriptorToXML(Paths.get(args[0]).toUri().toURL(), System.out);
	}

	public static void descriptorToXML(URL importDescriptor, OutputStream out) throws IOException, ParseException, XMLStreamException {
		ETLObservationSupplier suppl = ETLObservationSupplier.load(importDescriptor);
		GroupedFhirBundleWriter writer = new GroupedFhirBundleWriter(out);
		Meta.transfer(suppl, writer);
		Streams.transfer(suppl, writer);
		suppl.close();
		writer.close();
	}
}
